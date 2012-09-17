package com.wlanadb.service;

import com.wlanadb.config.MyConfig;
import com.wlanadb.utils.WiFiUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WiFiStateReceiver extends BroadcastReceiver {
  private static final String TAG = WiFiStateReceiver.class.getSimpleName();
  private static final boolean DEBUG = MyConfig.DEBUG && true;

  @Override
  public void onReceive(final Context context, final Intent intent) {
    final String action = intent.getAction();

    if (DEBUG)
      Log.d(TAG, "Action: " + action);

    if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
      final SupplicantState wifiState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
      if (DEBUG)
        Log.d(TAG, "Supplicant WiFi state changed: " + wifiState.name());
      setWiFiConnection(context, WiFiUtils.isWifiConnected(context));
    } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        || action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
      setWiFiConnection(context, WiFiUtils.isWifiConnected(context));
    }
  }

  private static void setWiFiConnection(Context context, boolean isConnected) {
    final Intent serviceIntent = new Intent(WLanAdbService.class.getName());
    if (isConnected) {
      context.startService(serviceIntent);
    } else {
      context.stopService(serviceIntent);
    }
  }
}
