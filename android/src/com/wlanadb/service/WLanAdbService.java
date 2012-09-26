package com.wlanadb.service;

import java.net.InetAddress;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.wlanadb.ApkInstallerActivity;
import com.wlanadb.config.MyConfig;
import com.wlanadb.data.Settings;
import com.wlanadb.data.CommandProto.Command;
import com.wlanadb.log.AnalyticsEvents;
import com.wlanadb.log.MyLog;
import com.wlanadb.logcat.PidsController;
import com.wlanadb.network.BroadcastServer;
import com.wlanadb.network.P2PServer;
import com.wlanadb.utils.WiFiUtils;
import com.wlanadb.worker.BaseWorker;
import com.wlanadb.worker.CommandProcessor;
import com.wlanadb.worker.InstallWorker;
import com.wlanadb.worker.LogcatWorker;
import com.wlanadb.worker.PushWorker;
import com.wlancat.service.WLanServiceApi;

public class WLanAdbService extends Service implements P2PServer.OnConnectionsCountChanged, CommandProcessor, Settings.OnSettingsChangeListener, AnalyticsEvents {
  private static final String TAG = WLanAdbService.class.getSimpleName();
  private static final boolean DEBUG = MyConfig.DEBUG && true;

  private GoogleAnalyticsTracker mTracker;

  private BroadcastServer mBroadcastServer;
  private UdpMessager mUdpMessager;
  private P2PServer mP2pServer;

  private PidsController mPidsController;
  private Settings mSettings;

  @Override
  public void onCreate() {
    super.onCreate();

    mTracker = GoogleAnalyticsTracker.getInstance();
    mTracker.startNewSession(MyConfig.GOOGLE_ANALITYCS_TRACKING_ID, getBaseContext());
    mTracker.setDispatchPeriod(30);
    //mTracker.setDebug(MyConfig.DEBUG);

    if (DEBUG)
      Log.d(TAG, "Starting service...");

    if (!WiFiUtils.isWifiAvailable(this)) {
      mTracker.trackEvent(CAT_WARNING, ACTION_STOP_SERVICE, LABEL_NO_WIFI, 0);
      if (DEBUG)
        Log.w(TAG, "WARNING! No WiFi available on device.");
      stopSelf();
      return;
    }

    if (!WiFiUtils.isWifiEnabled(this)) {
      mTracker.trackEvent(CAT_WARNING, ACTION_STOP_SERVICE, LABEL_WIFI_DISABLED, 0);
      if (DEBUG)
        Log.w(TAG, "WARNING! WiFi dissabled.");
      stopSelf();
      return;
    }

    mSettings = new Settings(getBaseContext());

    if (!isTrustedHotspotConnected()) {
      mTracker.trackEvent(CAT_WARNING, ACTION_STOP_SERVICE, LABEL_NOT_TRUSTED_HOTSPOT, 0);
      if (DEBUG)
        Log.w(TAG, "WARNING! Not trusted WiFi hotspot.");
      stopSelf();
      return;
    }

    start();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (DEBUG)
      Log.d(TAG, "Stoping service...");

    stop();

    mTracker.stopSession();
  }

  @Override
  public IBinder onBind(Intent intent) {
    if (this.getClass().getName().equals(intent.getAction())) {
      if (DEBUG)
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
      mTracker.trackEvent(CAT_WARNING, ACTION_STOP_SERVICE, LABEL_NO_LOCAL_ADDRESS, 0);
      if (DEBUG)
        Log.w(TAG, "Local address is NULL");
      stopSelf();
      return;
    }

    mTracker.trackEvent(CAT_WARNING, ACTION_START_SERVICE, LABEL_OK, 0);

    MyLog.init(getBaseContext());
    MyLog.v("Starting service...");

    mPidsController = new PidsController(getBaseContext());

    mSettings.startWatch();

    mP2pServer = new P2PServer(this);
    mP2pServer.start(this);

    mSettings.setIp(localAddress).setPort(mP2pServer.getPort()).commit();
    mSettings.addOnClientChangeListener(this);

    mUdpMessager = new UdpMessager(mSettings.getClient());

    mBroadcastServer = new BroadcastServer(mUdpMessager);
    final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    mBroadcastServer.start(wifiManager, broadcastAddress, localAddress);
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

    if (mSettings != null) {
      mSettings.stopWatch();
    }

    MyLog.v("Stoping service...");

    stopSelf();
  }

  private boolean isTrustedHotspotConnected() {
    if (!mSettings.isTrustedHotspotsEnabled())
      return true;

    final String ssid = WiFiUtils.getCurrentWifiConnectionSSID(getBaseContext());

    if (DEBUG) {
      Log.d(TAG, "- checking SSID to be trusted: " + ssid);
      for (String value : mSettings.getTrustedHotspots()) {
        Log.d(TAG, "--- " + value);
      }
    }

    final boolean isTrusted;
    if (ssid == null)
      isTrusted = false;
    else 
      isTrusted = mSettings.isThustedHotspot(ssid);

    return isTrusted;
  }

  @Override
  public void onSettingsChanged() {
    if (mUdpMessager != null)
      mUdpMessager.onClientChanged(mSettings.getClient());

    if (!isTrustedHotspotConnected())
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

    if (DEBUG) {
      Log.d(TAG, "  command: " + command.getCommand());
      Log.d(TAG, "  params: " + command.getParamsCount());
      for (String param : command.getParamsList()) {
        Log.d(TAG, "    param: " + param);
      }
      Log.d(TAG, "  checksum: " + command.getChecksum());
    }

    if (mSettings.hasPin()) {
      // pin was not provided to connect with device. terminating connection.
      if (!command.hasPin()) {
        return null;
      }

      // if pin is not correct. terminating connection
      if (!mSettings.checkPin(command.getPin())) {
        return null;
      }
    }

    final String comm = command.getCommand();
    if (comm.equals("logcat")) {
      mTracker.trackEvent(CAT_INFO, ACTION_COMMAND, LABEL_LOGCAT, 0);
      MyLog.w("Command: " + comm);

      final LogcatWorker logcatWorker = new LogcatWorker(command);
      logcatWorker.setPidsController(mPidsController);
      return logcatWorker;
    } else if (comm.equals("push")) {
      MyLog.w("Command: " + comm);
      mTracker.trackEvent(CAT_INFO, ACTION_COMMAND, LABEL_PUSH, 0);

      return new PushWorker(command);
    } else if (comm.equals("install")) {
      MyLog.w("Command: " + comm);

      mTracker.trackEvent(CAT_INFO, ACTION_COMMAND, LABEL_INSTALL, 0);

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
        public void onWorkerFinished() {
          final Intent installIntent = new Intent(getBaseContext(), ApkInstallerActivity.class);
          installIntent.setData(Uri.fromFile(installWorker.getApkFile()));
          installIntent.setFlags(Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_NEW_TASK);
          installIntent.putExtra(ApkInstallerActivity.EXTRA_LAUNCH_ON_SUCCESS, launch);
          startActivity(installIntent);
        }

        @Override
        public void onError() {
        }
      });
      return installWorker;
    } else {
      MyLog.w("Command: UNKNOWN");
      mTracker.trackEvent(CAT_INFO, ACTION_COMMAND, LABEL_UNKNOWN, 0);
      // we can't perform any action without specifying command.
      return null;
    }
  }

  private WLanServiceApi.Stub apiEndpoint = new WLanServiceApi.Stub() {

    @Override
    public int getPort() throws RemoteException {
      return mP2pServer == null ? -1 : mP2pServer.getPort();
    }

    @Override
    public String getAddress() throws RemoteException {
      final InetAddress localAddress = WiFiUtils.getLocalAddress(WLanAdbService.this);
      return localAddress.getHostAddress();
    }

    @Override
    public int getConnectionsCount() throws RemoteException {
      return mP2pServer == null ? 0 : mP2pServer.getActiveConnectionsCount();
    }
  };
}
