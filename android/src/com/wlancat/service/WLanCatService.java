package com.wlancat.service;

import java.net.InetAddress;

import com.wlancat.ApkInstallerActivity;
import com.wlancat.data.ClientSettings;
import com.wlancat.data.CommandProto.Command;
import com.wlancat.logcat.PidsController;
import com.wlancat.network.BroadcastServer;
import com.wlancat.network.P2PServer;
import com.wlancat.network.P2PServer.OnConnectionsCountChanged;
import com.wlancat.utils.WiFiUtils;
import com.wlancat.worker.BaseWorker;
import com.wlancat.worker.CommandProcessor;
import com.wlancat.worker.InstallWorker;
import com.wlancat.worker.LogcatWorker;
import com.wlancat.worker.PushWorker;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class WLanCatService extends Service implements OnConnectionsCountChanged, CommandProcessor {
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

    if (localAddress == null) {
      stopSelf();
      return;
    }

    mClientSettings = new ClientSettings(this);
    mClientSettings.start();

    mP2pServer = new P2PServer(this);
    mP2pServer.start(this);

    mClientSettings.setIp(localAddress).setPort(mP2pServer.getPort()).commit();

    mUdpMessager = new UdpMessager();
    mClientSettings.addOnClientChangeListener(mUdpMessager);

    mBroadcastServer = new BroadcastServer(mUdpMessager);
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

  @Override
  public void onConnectionsCountChanged(int connectionsCount) {
    final Intent i = new Intent(ConnectionsStatusReciever.ACTION_CONNECTIONS_COUNT);
    i.putExtra(ConnectionsStatusReciever.EXTRA_CONNECTIONS_COUNT, connectionsCount);
    sendBroadcast(i);
  }

  @Override
  public BaseWorker getWorker(Command command) {
    if (command == null)
      return null;

    Log.d(TAG, "  command: " + command.getCommand());
    Log.d(TAG, "  params: " + command.getParamsCount());
    for (String param : command.getParamsList()) {
      Log.d(TAG, "    param: " + param);
    }
    Log.d(TAG, "  checksum: " + command.getChecksum());

    if (mClientSettings.hasPin()) {
      // pin was not provided to connect with device. terminating connection.
      if (!command.hasPin()) {
        return null;
      }

      // if pin is not correct. terminating connection
      if (!mClientSettings.checkPin(command.getPin())) {
        return null;
      }
    }

    final String comm = command.getCommand();
    final BaseWorker worker;
    if (comm.equals("logcat")) {
      final LogcatWorker logcatWorker = new LogcatWorker(command);
      logcatWorker.setPidsController(new PidsController(this));
      worker = logcatWorker;
    } else if (comm.equals("push")) {
      return new PushWorker(command);
    } else if (comm.equals("install")) {
      final InstallWorker installWorker = new InstallWorker(command);

      boolean hasLaunchParam = false;
      for (String param : command.getParamsList()) {
        if (param.equals("-l")) {
          hasLaunchParam = true;
          break;
        }
      }

      final boolean launch = hasLaunchParam;

      installWorker.setWorkerListener(new BaseWorker.WorkerListener() {
        @Override
        public void onSuccess() {
          final Intent installIntent = new Intent(getBaseContext(), ApkInstallerActivity.class);
          installIntent.setData(Uri.fromFile(installWorker.getApkFile()));
          installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          installIntent.putExtra(ApkInstallerActivity.EXTRA_LAUNCH_ON_SUCCESS, launch);
          startActivity(installIntent);
        }

        @Override
        public void onError() {
        }
      });
      worker = installWorker;
    } else {
      // we can't perform any action without specifying command.
      worker = null;
    }

    return worker;
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
