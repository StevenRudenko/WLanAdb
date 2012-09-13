package com.wlanadb.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.util.Log;

public class InetAddressUtils {
  private static final String TAG = InetAddressUtils.class.getSimpleName();

  /**
   * Returns {@link InetAddress} object created from string address value.
   * @param address - address value to parse.
   * @return {@link InetAddress} object created from string address value.
   */
  public static InetAddress parseAddress(String address) {
    if (address == null)
      return null;

    try {
      return InetAddress.getByName(address);
    } catch (UnknownHostException e) {
      Log.e(TAG, "Can't parse address: " + address, e);
    }
    return null;
  }
}
