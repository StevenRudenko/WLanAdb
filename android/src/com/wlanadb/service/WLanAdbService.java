package com.wlanadb.service;

import java.net.InetAddress;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.google.protobuf.ByteString;
import com.wlanadb.ApkInstallerActivity;
import com.wlanadb.config.MyConfig;
import com.wlanadb.config.SettingsManager;
import com.wlanadb.data.CommandProto.Command;
import com.wlanadb.log.AnalyticsEvents;
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

public class WLanAdbService extends Service implements P2PServer.OnConnectionsCountChanged, CommandProcessor, SettingsManager.OnSettingsChangeListener, AnalyticsEvents, BroadcastServer.TestConnectionListener {
    private static final String TAG = WLanAdbService.class.getSimpleName();
    private static final boolean DEBUG = MyConfig.DEBUG && true;

    private Tracker mTracker;

    private BroadcastServer mBroadcastServer;
    private UdpMessager mUdpMessager;
    private P2PServer mP2pServer;
    private WifiManager.WifiLock mWifiLock;

    private PidsController mPidsController;
    private SettingsManager mSettings;

    @Override
    public void onCreate() {
        super.onCreate();

        final GoogleAnalytics analytics = GoogleAnalytics.getInstance(getApplicationContext());
        analytics.setDebug(MyConfig.DEBUG);
        mTracker = analytics.getTracker(MyConfig.GOOGLE_ANALITYCS_TRACKING_ID);
        mTracker.setStartSession(true);

        if (DEBUG)
            Log.d(TAG, "Starting service...");

        if (!WiFiUtils.isWifiAvailable(this)) {
            mTracker.trackEvent(CAT_SERVICE, ACTION_STOP_SERVICE, LABEL_NO_WIFI, 0L);
            if (DEBUG)
                Log.w(TAG, "WARNING! No WiFi available on device.");
            stopSelf();
            return;
        }

        if (!WiFiUtils.isWifiEnabled(this)) {
            mTracker.trackEvent(CAT_SERVICE, ACTION_STOP_SERVICE, LABEL_WIFI_DISABLED, 0L);
            if (DEBUG)
                Log.w(TAG, "WARNING! WiFi dissabled.");
            stopSelf();
            return;
        }

        mSettings = new SettingsManager(getBaseContext());

        if (!isTrustedHotspotConnected()) {
            mTracker.trackEvent(CAT_SERVICE, ACTION_STOP_SERVICE, LABEL_NOT_TRUSTED_HOTSPOT, 0L);
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

        mTracker.close();
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
            mTracker.trackEvent(CAT_SERVICE, ACTION_STOP_SERVICE, LABEL_NO_LOCAL_ADDRESS, 0L);
            if (DEBUG)
                Log.w(TAG, "Local address is NULL");
            stopSelf();
            return;
        }

        mTracker.trackEvent(CAT_SERVICE, ACTION_START_SERVICE, LABEL_OK, 0L);

        final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        final int wifiMode;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            wifiMode = WifiManager.WIFI_MODE_FULL;
        } else {
            wifiMode = WifiManager.WIFI_MODE_FULL_HIGH_PERF;
        }
        mWifiLock = wifiManager.createWifiLock(wifiMode, TAG);
        mPidsController = new PidsController(getBaseContext());

        mSettings.startWatch(true);

        mP2pServer = new P2PServer(this);
        mP2pServer.start(this);

        mSettings.setIp(localAddress).setPort(mP2pServer.getPort()).commit();
        mSettings.addOnClientChangeListener(this);

        mUdpMessager = new UdpMessager(mSettings.getClient());

        mBroadcastServer = new BroadcastServer(mUdpMessager);
        mBroadcastServer.start(wifiManager, broadcastAddress, localAddress);

        if (mSettings.getWifiLockEnabled()) {
            mWifiLock.acquire();
        }
    }

    private void stop() {
        if (mWifiLock != null && mWifiLock.isHeld()) {
            mWifiLock.release();
        }

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
        if (MyConfig.DEBUG)
            Log.d(TAG, "Settings were changed...");

        if (!isTrustedHotspotConnected()) {
            stopSelf();
            return;
        }

        if (mUdpMessager != null)
            mUdpMessager.onClientChanged(mSettings.getClient());

        if (mSettings.getWifiLockEnabled()) {
            if (!mWifiLock.isHeld())
                mWifiLock.acquire();
        } else if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    @Override
    public void onConnectionsCountChanged(int connectionsCount) {
        final Intent i = new Intent(ConnectionsStatusReciever.ACTION_CONNECTIONS_CHANGED);
        i.putExtra(ConnectionsStatusReciever.EXTRA_CONNECTIONS_COUNT, connectionsCount);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    @Override
    public void onTestCompleted(int status) {
        final Intent i = new Intent(ConnectionsStatusReciever.ACTION_CONNECTIONS_CHANGED);
        i.putExtra(ConnectionsStatusReciever.EXTRA_CONNECTIONS_STATUS, status);
        i.putExtra(ConnectionsStatusReciever.EXTRA_CONNECTIONS_COUNT, getP2PConnectionsCount());
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    @Override
    public BaseWorker getWorker(Command command) {
        if (command == null) {
            if (DEBUG)
                Log.d(TAG, "Fail to create worker: command is null");
            return null;
        }

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
            mTracker.trackEvent(CAT_COMMAND, ACTION_COMMAND, LABEL_LOGCAT, 0L);

            final LogcatWorker logcatWorker = new LogcatWorker(command);
            logcatWorker.setPidsController(mPidsController);
            return logcatWorker;
        } else if (comm.equals("push")) {
            mTracker.trackEvent(CAT_COMMAND, ACTION_COMMAND, LABEL_PUSH, 0L);

            final PushWorker worker = new PushWorker(command);
            worker.setWorkerListener(new BaseWorker.WorkerListener() {
                @Override
                public void onWorkerFinished() {
                    //TODO: add notification
                }

                @Override
                public void onError() {
                }
            });
            return worker;
        } else if (comm.equals("install")) {
            mTracker.trackEvent(CAT_COMMAND, ACTION_COMMAND, LABEL_INSTALL, 0L);

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
            mTracker.trackEvent(CAT_COMMAND, ACTION_COMMAND, LABEL_UNKNOWN, 0L);
            // we can't perform any action without specifying command.
            return null;
        }
    }

    private int getP2PConnectionsCount() {
        return mP2pServer == null ? 0 : mP2pServer.getActiveConnectionsCount();
    }

    private WLanServiceApi.Stub apiEndpoint = new WLanServiceApi.Stub() {

        @Override
        public int getPort() throws RemoteException {
            return mP2pServer == null ? -1 : mP2pServer.getPort();
        }

        @Override
        public String getAddress() throws RemoteException {
            final InetAddress localAddress = WiFiUtils.getLocalAddress(WLanAdbService.this);
            if ( localAddress == null )
                return null;
            return localAddress.getHostAddress();
        }

        @Override
        public int getConnectionsCount() throws RemoteException {
            return getP2PConnectionsCount();
        }

        @Override
        public void testConnection() throws RemoteException {
            if (mBroadcastServer != null)
                mBroadcastServer.testConnection(WLanAdbService.this);
        }
    };
}
