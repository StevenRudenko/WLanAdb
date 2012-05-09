package com.wlancat.service;

import java.net.InetAddress;

import net.sf.signalslot_apt.SignalSlot;
import net.sf.signalslot_apt.annotations.signalslot;
import net.sf.signalslot_apt.annotations.slot;

import com.wlancat.data.ClientSettings;
import com.wlancat.data.ClientSettingsSignalSlot;
import com.wlancat.network.BroadcastServer;
import com.wlancat.network.BroadcastServerSignalSlot;
import com.wlancat.network.P2PServer;
import com.wlancat.network.P2PServerSignalSlot;
import com.wlancat.utils.WiFiUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

@signalslot(force_concrete=true)
public class WLanCatService extends Service {
  private static final String TAG = WLanCatService.class.getSimpleName();

  private BroadcastServer mBroadcastServer;
  private UdpMessager mUdpMessager;
  private P2PServer mP2pServer;
  private ClientSettings mClientSettings;

  @Override
  public void onCreate() {
    super.onCreate();

    Log.d(TAG, "Starting service...");

    if (!WiFiUtils.isWifiAvailable(this)) {
      Log.w(TAG, "WARNING! No WiFi available on device.");
      stopSelf();
      return;
    }

    if (!WiFiUtils.isWifiEnabled(this)) {
      Log.w(TAG, "WARNING! WiFi dissabled.");
      stopSelf();
      return;
    }

    start();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d(TAG, "Service stops...");

    stop();
  }

  @Override
  public IBinder onBind(Intent intent) {
    if (this.getClass().getName().equals(intent.getAction())) {
      Log.d(TAG, "Bound by intent " + intent);
      return apiEndpoint;
    } else {
      return null;
    }
  }

  private void start() {
    final InetAddress broadcastAddress = WiFiUtils.getBroadcastAddress(this);
    final InetAddress localAddress = WiFiUtils.getLocalAddress(this);

    mClientSettings = new ClientSettingsSignalSlot(this);
    mClientSettings.start();

    mP2pServer = new P2PServerSignalSlot(mClientSettings);

    SignalSlot.connect(mP2pServer, P2PServerSignalSlot.Signals.ONACTIVECONNECTIONSCOUNTCHANGED_INT, this, WLanCatServiceSignalSlot.Slots.ONCONNECTIONSCOUNTCHANGED_INT);

    mP2pServer.start();

    mClientSettings.setIp(localAddress);
    mClientSettings.setPort(mP2pServer.getPort());

    mUdpMessager = new UdpMessagerSignalSlot(mClientSettings.getClient());

    SignalSlot.connect(mClientSettings, ClientSettingsSignalSlot.Signals.ONCLIENTCHANGED_CLIENT, mUdpMessager, UdpMessagerSignalSlot.Slots.ONCLIENTCHANGED_CLIENT);

    mBroadcastServer = new BroadcastServerSignalSlot();

    SignalSlot.connect(mBroadcastServer, BroadcastServerSignalSlot.Signals.ONDATAPACKAGE_BYTESTRING, mUdpMessager, UdpMessagerSignalSlot.Slots.ONMESSAGERECIEVED_BYTESTRING);
    SignalSlot.connect(mUdpMessager, UdpMessagerSignalSlot.Signals.SENDRESPONSE_BYTESTRING, mBroadcastServer, BroadcastServerSignalSlot.Slots.SEND_BYTESTRING);

    mBroadcastServer.start(broadcastAddress, localAddress);
  }

  private void stop() {
    if (mP2pServer != null) {
      mP2pServer.stop();
      mP2pServer = null;
    }

    if (mBroadcastServer != null) {
      mBroadcastServer.stop();
      mBroadcastServer = null;
    }

    if (mClientSettings != null) {
      mClientSettings.stop();
      mClientSettings = null;
    }

    stopSelf();
  }

  @slot
  public void onConnectionsCountChanged(int connectionsCount) {
    final Intent i = new Intent(ConnectionsStatusReciever.ACTION_CONNECTIONS_COUNT);
    i.putExtra(ConnectionsStatusReciever.EXTRA_CONNECTIONS_COUNT, connectionsCount);
    sendBroadcast(i);
  }

  private WLanServiceApi.Stub apiEndpoint = new WLanServiceApi.Stub() {

    @Override
    public int getPort() throws RemoteException {
      return mP2pServer != null ? mP2pServer.getPort() : -1;
    }

    @Override
    public String getAddress() throws RemoteException {
      final InetAddress localAddress = WiFiUtils.getLocalAddress(WLanCatService.this);
      return localAddress.getHostAddress();
    }

    @Override
    public int getConnectionsCount() throws RemoteException {
      return mP2pServer == null ? 0 : mP2pServer.getActiveConnectionsCount();
    }
  };
}
