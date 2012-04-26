package com.wlancat.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

public class WiFiUtils {
  private static final String TAG = WiFiUtils.class.getSimpleName();

  /**
   * Calculate the broadcast IP we need to send the packet along. If we send it
   * to 255.255.255.255, it never gets sent. I guess this has something to do
   * with the mobile network not wanting to do broadcast.
   * 
   * @param context
   *          - {@link Context} used to get WiFi manager
   */
  public static InetAddress getBroadcastAddress(Context context) {
    final WifiManager wifiManager = (WifiManager) context
        .getSystemService(Context.WIFI_SERVICE);
    DhcpInfo dhcp = wifiManager.getDhcpInfo();
    if (dhcp == null) {
      Log.d(TAG, "Could not get DHCP info");
      return null;
    }

    int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
    byte[] quads = new byte[4];
    for (int k = 0; k < 4; k++)
      quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
    try {
      return InetAddress.getByAddress(quads);
    } catch (UnknownHostException e) {
      Log.e(TAG, "Could not create address from DHCP info", e);
    }
    return null;
  }

  /**
   * Calculate the local IP of WiFi connection.
   * 
   * @param context - used to get WiFi manager
   */
  public static InetAddress getLocalAddress(Context context) {
    final WifiManager wifiManager = (WifiManager) context
        .getSystemService(Context.WIFI_SERVICE);

    final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    if (wifiInfo == null)
      return null;

    final int ipAddress = wifiInfo.getIpAddress();
    final String ipAddressFormated = Formatter.formatIpAddress(ipAddress);

    try {
      return InetAddress.getByName(ipAddressFormated);
    } catch (UnknownHostException e) {
      Log.e(TAG, "Can't create inet address", e);
    }
    return null;
  }

  /**
   * Returns MAC address of WiFi module.
   * 
   * @param context - used to get WiFi manager
   */
  public static String getMacAddress(Context context) {
    final WifiManager wifiManager = (WifiManager) context
        .getSystemService(Context.WIFI_SERVICE);

    final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    if (wifiInfo == null)
      return null;

    return wifiInfo.getMacAddress().toUpperCase();
  }

  /**
   * Returns true if WiFi connection was established
   * @param context - used to get connectivity manger
   * @return true if WiFi connection established, otherwise - false.
   */
  public static boolean isWifiConnected(Context context) {
    final WifiManager wifiManager = (WifiManager) context
        .getSystemService(Context.WIFI_SERVICE);

    final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    if (wifiInfo == null)
      return false;

    return wifiInfo.getIpAddress() != 0;
  }

  /**
   * Returns true if WiFi available on device
   * @param context - used to get WiFi manger
   * @return true if WiFi available on device, otherwise - false.
   */
  public static boolean isWifiAvailable(Context context) {
    final WifiManager wifiManager = (WifiManager) context
        .getSystemService(Context.WIFI_SERVICE);
    return wifiManager != null;
  }

  /**
   * Returns true if WiFi is enabled
   * @param context - used to get WiFi manger
   * @return true if WiFi is enabled on device, otherwise - false.
   */
  public static boolean isWifiEnabled(Context context) {
    final WifiManager wifiManager = (WifiManager) context
        .getSystemService(Context.WIFI_SERVICE);
    if (wifiManager == null)
      return false;
    return wifiManager.isWifiEnabled();
  }
}
