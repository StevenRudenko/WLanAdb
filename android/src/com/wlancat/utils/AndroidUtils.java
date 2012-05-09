package com.wlancat.utils;

import java.io.File;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.provider.Settings.Secure;

public class AndroidUtils {

  public static String getAndroidId(Context context) {
    return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID); 
  }

  public static File getSharedPreferencesFile(Context context, String prefFilename) {
    final ApplicationInfo applicationInfo = context.getApplicationInfo();
    if (applicationInfo == null) return null;
    if (applicationInfo.dataDir == null) return null;
    final File dataDir = new File(applicationInfo.dataDir);
    final File sharedPrefsDir = new File(dataDir, "shared_prefs");

    final String filename = prefFilename == null ? applicationInfo.packageName + "_preferences.xml" : prefFilename;
    final File sharedPrefFile = new File(sharedPrefsDir, filename);
    return sharedPrefFile.exists() && sharedPrefFile.isFile() ? sharedPrefFile : null;
  }
}
