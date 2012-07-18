package com.wlancat.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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

  public static List<RunningProcess> getRunningProcesses(Context context) {
    final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    final PackageManager pm = context.getPackageManager();

    final List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
    if (list == null)
      return null;

    final List<RunningProcess> result = new ArrayList<RunningProcess>();
    for (ActivityManager.RunningAppProcessInfo info : list) {
      final ApplicationInfo appInfo;
      try {
        appInfo = pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA);
      } catch (NameNotFoundException e) {
        continue;
      }

      final CharSequence name = pm.getApplicationLabel(appInfo);
      result.add(new RunningProcess(name, info.pid, info.uid));
    }
    return result;
  }

  public static class RunningProcess {
    public final CharSequence name;
    public final int pid;
    public final int uid;

    private RunningProcess(CharSequence name, int pid, int uid) {
      this.name = name;
      this.pid = pid;
      this.uid = uid;
    }
  }
}
