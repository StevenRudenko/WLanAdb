package com.wlanadb.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

public class HashHelper {
  public static final String TAG = HashHelper.class.getSimpleName();

  public static String getHashString(String key) {
    if (TextUtils.isEmpty(key))
      return null;

    final byte[] shaBytes = getHashToBytes(key);
    final String shaString = convertToHex(shaBytes);
    if (TextUtils.isEmpty(shaString))
      return null;

    final String base64String;
    base64String = new String(Base64.encodeToString(shaBytes, Base64.DEFAULT));
    return base64String.trim();
  }

  public static String convertToHex(byte[] data) {
    if (data == null || data.length == 0)
      return null;

    final StringBuffer buf = new StringBuffer();
    for (int i = 0; i < data.length; i++) {
      int halfbyte = (data[i] >>> 4) & 0x0F;
      int two_halfs = 0;
      do {
        if ((0 <= halfbyte) && (halfbyte <= 9))
          buf.append((char) ('0' + halfbyte));
        else
          buf.append((char) ('a' + (halfbyte - 10)));
        halfbyte = data[i] & 0x0F;
      } while (two_halfs++ < 1);
    }
    return buf.toString();
  }

  private static byte[] getHashToBytes(String key) {
    try {
      final MessageDigest sha = MessageDigest.getInstance("SHA-1");
      byte[] sha1hash = null;
      sha.update(key.getBytes("UTF-8"), 0, key.length());
      sha1hash = sha.digest();
      return sha1hash;
    } catch (NoSuchAlgorithmException e) {
      Log.e(TAG,"Can't get hash for string!", e);
    } catch (UnsupportedEncodingException e) {
      Log.e(TAG,"Encoding can not be performed!", e);
    }
    return null;
  }
}