package com.wlanadb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.wlanadb.actionbar.ActionBarPreferenceActivity;
import com.wlanadb.data.ClientProto.Client;
import com.wlanadb.data.Settings;
import com.wlanadb.fragment.LicensesDialogFragment;
import com.wlanadb.ui.prefs.PasswordPreference;
import com.wlanadb.ui.prefs.SharedPreferencesHelper;
import com.wlanadb.ui.prefs.SwitchPreference;

public class SettingsActivity extends ActionBarPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Settings.OnSettingsChangeListener {

  private String PREF_CLIENT_ID;
  private String PREF_CLIENT_NAME;
  private String PREF_SECURITY_PIN;
  private String PREF_SECURITY_TRUSTED_HOTSPOTS;
  private String PREF_ABOUT_CREDITS;
  private String PREF_ABOUT_LICENSE;

  private Settings mSettings;

  private EditTextPreference mClientIdPref;
  private EditTextPreference mClientNamePref;

  private PasswordPreference mSecuityPinPref;
  private Preference mSecurityTrustedHotspotsPref;

  @SuppressWarnings("deprecation")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    PREF_CLIENT_ID = getString(R.string.pref_client_id);
    PREF_CLIENT_NAME = getString(R.string.pref_client_name);
    PREF_SECURITY_PIN = getString(R.string.pref_security_pin);
    PREF_SECURITY_TRUSTED_HOTSPOTS = getString(R.string.pref_security_trusted_hotspots);
    PREF_ABOUT_CREDITS = getString(R.string.pref_about_credits);
    PREF_ABOUT_LICENSE = getString(R.string.pref_about_license);

    addPreferencesFromResource(R.xml.preference_settings);

    final PreferenceScreen screen = getPreferenceScreen();
    mClientIdPref = (EditTextPreference) screen.findPreference(PREF_CLIENT_ID);
    mClientNamePref = (EditTextPreference) screen.findPreference(PREF_CLIENT_NAME);

    mSecuityPinPref = (PasswordPreference) screen.findPreference(PREF_SECURITY_PIN);
    mSecurityTrustedHotspotsPref = screen.findPreference(PREF_SECURITY_TRUSTED_HOTSPOTS);
    final Intent intentTrustedHotspots = new Intent(getBaseContext(), TrustedHotspotsActivity.class);
    mSecurityTrustedHotspotsPref.setIntent(intentTrustedHotspots);

    final Preference prefLicense = screen.findPreference(PREF_ABOUT_LICENSE);
    prefLicense.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        LicensesDialogFragment.createDialog(SettingsActivity.this).show();
        return true;
      }
    });

    final Preference prefCredits = screen.findPreference(PREF_ABOUT_CREDITS);
    prefCredits.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        //TODO
        return true;
      }
    });

    mSettings = new Settings(getBaseContext());
    mSettings.addOnClientChangeListener(this);
  }

  @Override
  protected void onResume() {
    super.onResume();

    // clearing preferences to avoid conflicts
    clearPreferences();
    // starting listen for global settings changes
    mSettings.refresh();
    mSettings.startWatch();
    onSettingsChanged();
    // starting listen for preferences changes
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    prefs.registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  protected void onPause() {
    super.onPause();

    // finishing listen for global settings changes
    mSettings.stopWatch();
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
          final SwitchPreference pref = (SwitchPreference) mSecurityTrustedHotspotsPref;
          pref.setChecked(mSettings.isTrustedHotspotsEnabled());
        } else {
          final CheckBoxPreference pref = (CheckBoxPreference) mSecurityTrustedHotspotsPref;
          pref.setChecked(mSettings.isTrustedHotspotsEnabled());
        }
      }
    });
  }

  private void clearPreferences() {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    final SharedPreferences.Editor prefsEraser = prefs.edit();
    // removing client preferences only
    prefsEraser.remove(PREF_CLIENT_ID).remove(PREF_CLIENT_NAME).remove(PREF_SECURITY_PIN).remove(PREF_SECURITY_TRUSTED_HOTSPOTS);
    SharedPreferencesHelper.apply(prefsEraser);
  }
}