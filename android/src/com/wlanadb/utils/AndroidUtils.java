package com.wlanadb.utils;

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
    final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    final PackageManager pm = context.getPackageManager();

    return getRunningProcesses(am, pm);
  }

  public static List<RunningProcess> getRunningProcesses(ActivityManager am, PackageManager pm) {
    final List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();
    if (list == null)
      return null;

    final List<RunningProcess> result = new ArrayList<RunningProcess>();
    for (ActivityManager.RunningAppProcessInfo info : list) {
      String name = null;
      try {
        final ApplicationInfo appInfo = pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA);
        name = pm.getApplicationLabel(appInfo).toString();
      } catch (NameNotFoundException e) {
        name = info.processName;
      }

      result.add(new RunningProcess(name, info.processName, info.pid, info.uid));
    }
    return result;
  }

  public static class RunningProcess {
    public final String name;
    public final String processName;
    public final int pid;
    public final int uid;

    private RunningProcess(String name, String processName, int pid, int uid) {
      this.name = name;
      this.processName = processName;
      this.pid = pid;
      this.uid = uid;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof RunningProcess))
        return false;

      final RunningProcess casted = (RunningProcess) o;
      return casted.pid == pid && casted.uid == uid;
    }

    @Override
    public int hashCode() {
      return pid;
    }
  }
}
