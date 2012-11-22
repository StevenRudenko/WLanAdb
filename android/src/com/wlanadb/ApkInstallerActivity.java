package com.wlanadb;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.wlanadb.config.MyConfig;
import com.wlanadb.config.SettingsManager;

public class ApkInstallerActivity extends Activity {
  private static final String TAG = ApkInstallerActivity.class.getSimpleName();
  private static final boolean DEBUG = MyConfig.DEBUG && true;

  private static final int SUBACTIVITY_INSTALLER = 100;

  public static final String EXTRA_LAUNCH_ON_SUCCESS = TAG+".start_on_success";

  @SuppressWarnings("deprecation")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final Intent intent = getIntent();
    final Uri apkFileUri = intent.getData();

    final SettingsManager settings = new SettingsManager(getBaseContext());
    final boolean askToInstall = settings.getAskToInstall();

    if (!askToInstall && installSilently(apkFileUri)) {
      launchApp(apkFileUri);
      finish();
      return;
    }

    final Intent promptInstall;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      promptInstall = new Intent(Intent.ACTION_INSTALL_PACKAGE);
      promptInstall.setData(apkFileUri);
    } else {
      promptInstall = new Intent(Intent.ACTION_VIEW);
      promptInstall.setDataAndType(apkFileUri, "application/vnd.android.package-archive"); 
    }
    promptInstall.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
    promptInstall.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

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

      launchApp(apkFileUri);
    }

    finish();
  }

  private void launchApp(Uri apkFileUri) {
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

  private static boolean installSilently(Uri apkFileUri) {
    if (!hasRootAccess())
      return false;

    try {
      if (DEBUG)
        Log.d(TAG, "Trying install APK silently...");
      final int result = Runtime.getRuntime().exec("pm install " + apkFileUri.getPath()).waitFor();
      if (DEBUG)
        Log.w(TAG, "Silent install process " + (result == 0 ? "SUCCESSFUL" : "FAILED"));
      return result == 0;
    } catch (InterruptedException e) {
      Log.e(TAG, "Silent install process interrupted", e);
    } catch (IOException e) {
      Log.e(TAG, "Fail to perform silent install", e);
    } catch (Exception e) {
      if (DEBUG)
        Log.w(TAG, "Silent install process FAILED", e);
    }
    return false;
  }

  @SuppressWarnings("deprecation")
  public static boolean hasRootAccess() {
    boolean hasRoot = false;

    try {
      final Process su = Runtime.getRuntime().exec("su");

      final DataOutputStream os = new DataOutputStream(su.getOutputStream());
      final DataInputStream is = new DataInputStream(su.getInputStream());

      if (os != null && is != null) {
        // Getting current user's UID to check for Root Access
        os.writeBytes("id\n");
        os.flush();

        final String id = is.readLine();
        boolean exitSu = false;
        if (id == null) {
          hasRoot = false;
          exitSu = false;
          if (DEBUG)
            Log.d(TAG, "Can't get Root Access or it was denied by user");
        } else if (id.contains("uid=0")) {
          //If is contains uid=0, It means Root Access is granted
          hasRoot = true;
          exitSu = true;
          if (DEBUG)
            Log.d(TAG, "Root Access granted");
        } else {
          hasRoot = false;
          exitSu = true;
          if (DEBUG)
            Log.d(TAG, "Root Access rejected: " + is.readLine());
        }

        if (exitSu) {
          os.writeBytes("exit\n");
          os.flush();
        }
      }
    } catch (Exception e) {
      hasRoot = false;
      if (DEBUG)
        Log.d(TAG, "Root Access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
    }

    return hasRoot;
  }

}
