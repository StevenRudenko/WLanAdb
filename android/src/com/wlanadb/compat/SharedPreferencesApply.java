package com.wlanadb.compat;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;

public class SharedPreferencesApply {

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
