package com.wlanadb.ui.prefs;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

public class SharedPreferencesHelper {

  public static String getDefaultPrefFilePath(Context context) {
    final String packageName = context.getPackageName();
    return "/data/data/"+packageName+"/shared_prefs/"+packageName+"_preferences.xml";
  }

  public static String getPrefFilePath(Context context, String filename) {
    final String packageName = context.getPackageName();
    return "/data/data/"+packageName+"/shared_prefs/"+filename+".xml";
  }

  public static void apply(SharedPreferences.Editor editor) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD)
      applyPre9(editor);
    else
      applyPost9(editor);
  }

  private static void applyPre9(SharedPreferences.Editor editor) {
    editor.commit();
  }

  @TargetApi(9)
  private static void applyPost9(SharedPreferences.Editor editor) {
    editor.apply();
  }
}
