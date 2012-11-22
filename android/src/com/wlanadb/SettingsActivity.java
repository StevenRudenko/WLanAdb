package com.wlanadb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.wlanadb.actionbar.ActionBarPreferenceActivity;
import com.wlanadb.config.SettingsManager;
import com.wlanadb.data.ClientProto.Client;
import com.wlanadb.fragment.EnableWifiDialogFragment;
import com.wlanadb.fragment.LicensesDialogFragment;
import com.wlanadb.ui.prefs.CheckBoxPreference;
import com.wlanadb.ui.prefs.PasswordPreference;
import com.wlanadb.ui.prefs.SharedPreferencesHelper;
import com.wlanadb.ui.prefs.SwitchPreference;
import com.wlanadb.utils.WiFiUtils;

public class SettingsActivity extends ActionBarPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, SettingsManager.OnSettingsChangeListener {

  private String PREF_CLIENT_ID;
  private String PREF_CLIENT_NAME;
  private String PREF_SECURITY_PIN;
  private String PREF_SECURITY_TRUSTED_HOTSPOTS;
  private String PREF_SECURITY_ASK_INSTALL;
  private String PREF_SETTINGS_WIFI_LOCK;
  @SuppressWarnings("unused")
  private String PREF_ABOUT_CREDITS;
  private String PREF_ABOUT_LICENSE;

  private SettingsManager mSettings;

  private EditTextPreference mClientIdPref;
  private EditTextPreference mClientNamePref;

  private PasswordPreference mSecuityPinPref;
  private Preference mSecurityTrustedHotspotsPref;
  private android.preference.CheckBoxPreference mAskInstallPref;

  private android.preference.CheckBoxPreference mWifiLock;

  @SuppressWarnings("deprecation")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    PREF_CLIENT_ID = getString(R.string.pref_client_id);
    PREF_CLIENT_NAME = getString(R.string.pref_client_name);
    PREF_SECURITY_PIN = getString(R.string.pref_security_pin);
    PREF_SECURITY_TRUSTED_HOTSPOTS = getString(R.string.pref_security_trusted_hotspots);
    PREF_SECURITY_ASK_INSTALL = getString(R.string.pref_security_ask_install);
    PREF_SETTINGS_WIFI_LOCK = getString(R.string.pref_settings_wifi_lock);
    PREF_ABOUT_CREDITS = getString(R.string.pref_about_credits);
    PREF_ABOUT_LICENSE = getString(R.string.pref_about_license);

    addPreferencesFromResource(R.xml.preference_settings);

    final PreferenceScreen screen = getPreferenceScreen();
    mClientIdPref = (EditTextPreference) screen.findPreference(PREF_CLIENT_ID);
    mClientNamePref = (EditTextPreference) screen.findPreference(PREF_CLIENT_NAME);

    mSecuityPinPref = (PasswordPreference) screen.findPreference(PREF_SECURITY_PIN);
    mSecurityTrustedHotspotsPref = screen.findPreference(PREF_SECURITY_TRUSTED_HOTSPOTS);
    mAskInstallPref = (android.preference.CheckBoxPreference) screen.findPreference(PREF_SECURITY_ASK_INSTALL);

    mWifiLock = (android.preference.CheckBoxPreference) screen.findPreference(PREF_SETTINGS_WIFI_LOCK);

    final Preference prefLicense = screen.findPreference(PREF_ABOUT_LICENSE);
    prefLicense.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        LicensesDialogFragment.createDialog(SettingsActivity.this).show();
        return true;
      }
    });

    final boolean hasRoot = ApkInstallerActivity.hasRootAccess();
    mAskInstallPref.setEnabled(hasRoot);

    mSettings = new SettingsManager(getBaseContext());
    mSettings.addOnClientChangeListener(this);
  }

  @Override
  protected void onResume() {
    super.onResume();

    // if WiFi is not enabled we will get null instead of list of hotspots
    // it doesn't make sense to show trusted hotspots list activity
    if (!WiFiUtils.isWifiEnabled(getBaseContext())) {
      mSecurityTrustedHotspotsPref.setIntent(null);
      mSecurityTrustedHotspotsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          EnableWifiDialogFragment.createDialog(SettingsActivity.this, EnableWifiDialogFragment.MSG_TRUSTED_HOTSPOTS).show();
          return true;
        }
      });
    } else {
      final Intent intentTrustedHotspots = new Intent(getBaseContext(), TrustedHotspotsActivity.class);
      mSecurityTrustedHotspotsPref.setIntent(intentTrustedHotspots);
      mSecurityTrustedHotspotsPref.setOnPreferenceClickListener(null);
    }

    // clearing preferences to avoid conflicts
    clearPreferences();
    // refreshing global settings changes
    mSettings.refresh();
    onSettingsChanged();
    // starting listen for preferences changes
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    prefs.registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  protected void onPause() {
    super.onPause();

    // finishing listen for preferences changes
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    prefs.unregisterOnSharedPreferenceChangeListener(this);
    // clearing preferences to avoid conflicts
    clearPreferences();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (PREF_CLIENT_NAME.equals(key)) {
      mSettings.setName(sharedPreferences.getString(PREF_CLIENT_NAME, null)).commit();
    } else if (PREF_SECURITY_PIN.equals(key)) {
      final String pin = sharedPreferences.getString(PREF_SECURITY_PIN, null);
      mSettings.setPin(pin).commit();
    } else if (PREF_SECURITY_TRUSTED_HOTSPOTS.equals(key)) {
      mSettings.setTrustedHotspotsEnabled(sharedPreferences.getBoolean(PREF_SECURITY_TRUSTED_HOTSPOTS, false)).commit();
    } else if (PREF_SECURITY_ASK_INSTALL.equals(key)) {
      mSettings.setAskToInstall(sharedPreferences.getBoolean(PREF_SECURITY_ASK_INSTALL, true)).commit();
    } else if (PREF_SETTINGS_WIFI_LOCK.equals(key)) {
      mSettings.setWifiLockEnabled(sharedPreferences.getBoolean(PREF_SETTINGS_WIFI_LOCK, false)).commit();
    }

    onSettingsChanged();
  }

  @Override
  public void onSettingsChanged() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final Client client = mSettings.getClient();
        mClientIdPref.setSummary(client.getId());
        mClientNamePref.setSummary(client.getName());
        mClientNamePref.setText(client.getName());
        mSecuityPinPref.setSummary(client.getUsePin() ? R.string.pref_security_pin_summary_set : R.string.pref_security_pin_summary_not_set);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
          final SwitchPreference trustedHotspotsPref = (SwitchPreference) mSecurityTrustedHotspotsPref;
          trustedHotspotsPref.setChecked(mSettings.isTrustedHotspotsEnabled());
        } else {
          final CheckBoxPreference trustedHotspotsPref = (CheckBoxPreference) mSecurityTrustedHotspotsPref;
          trustedHotspotsPref.setChecked(mSettings.isTrustedHotspotsEnabled());
        }
        mAskInstallPref.setChecked(mSettings.getAskToInstall());
        mWifiLock.setChecked(mSettings.getWifiLockEnabled());
      }
    });
  }

  private void clearPreferences() {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    final SharedPreferences.Editor prefsEraser = prefs.edit();
    // removing client preferences only
    prefsEraser.remove(PREF_CLIENT_ID)
    .remove(PREF_CLIENT_NAME)
    .remove(PREF_SECURITY_PIN)
    .remove(PREF_SECURITY_TRUSTED_HOTSPOTS)
    .remove(PREF_SECURITY_ASK_INSTALL)
    .remove(PREF_SETTINGS_WIFI_LOCK);
    SharedPreferencesHelper.apply(prefsEraser);
  }
}