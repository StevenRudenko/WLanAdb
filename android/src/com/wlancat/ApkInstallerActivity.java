package com.wlancat;

import com.wlancat.config.MyConfig;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class ApkInstallerActivity extends Activity {
  private static final String TAG = ApkInstallerActivity.class.getSimpleName();
  private static final boolean DEBUG = MyConfig.DEBUG && false;

  private static final int SUBACTIVITY_INSTALLER = 100;

  public static final String EXTRA_LAUNCH_ON_SUCCESS = TAG+".start_on_success";

  @SuppressWarnings("deprecation")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final Intent intent = getIntent();
    final Uri apkFileUri = intent.getData();

    final Intent promptInstall = new Intent(Intent.ACTION_INSTALL_PACKAGE);
    promptInstall.setData(apkFileUri);
    promptInstall.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
    //promptInstall.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

    final boolean launchOnSuccess = intent.getBooleanExtra(EXTRA_LAUNCH_ON_SUCCESS, true);
    if (launchOnSuccess) {
      if (DEBUG)
        Log.d(TAG, "Registering to get result of installation...");
      promptInstall.putExtra(Intent.EXTRA_RETURN_RESULT, launchOnSuccess);
      startActivityForResult(promptInstall, SUBACTIVITY_INSTALLER);
    } else {
      startActivity(promptInstall);
      finish();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (DEBUG)
      Log.d(TAG, "Install activity result: " + resultCode);

    if (requestCode == SUBACTIVITY_INSTALLER && resultCode == RESULT_OK) {
      final Intent intent = getIntent();
      final Uri apkFileUri = intent.getData();

      final PackageInfo pi = getPackageManager().getPackageArchiveInfo(apkFileUri.getPath(), 0);
      if (pi != null) {
        if (DEBUG)
          Log.d(TAG, "Launching installed application...");
        final Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pi.packageName);
        startActivity(launchIntent);
      } else {
        if (DEBUG)
          Log.w(TAG, "Fail to parse APK package to get package name to launch installed application");
      }
    }

    finish();
  }

}
