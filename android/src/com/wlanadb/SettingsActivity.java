package com.wlanadb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.wlanadb.actionbar.ActionBarPreferenceActivity;
import com.wlanadb.compat.SharedPreferencesHelper;
import com.wlanadb.data.ClientProto.Client;
import com.wlanadb.data.ClientSettings;
import com.wlanadb.data.ClientSettings.OnClientChangeListener;
import com.wlanadb.ui.prefs.PasswordPreference;
import com.wlanadb.ui.prefs.Preferences;
import com.wlanadb.ui.prefs.SwitchPreference;

public class SettingsActivity extends ActionBarPreferenceActivity implements OnSharedPreferenceChangeListener, OnClientChangeListener, Preferences {

  private ClientSettings mClientSettings;

  private EditTextPreference mClientIdPref;
  private EditTextPreference mClientNamePref;

  private PasswordPreference mSecuityPinPref;
  private Preference mSecurityTrustedHotspotsPref;

  @SuppressWarnings("deprecation")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preference_settings);

    final PreferenceScreen screen = getPreferenceScreen();
    mClientIdPref = (EditTextPreference) screen.findPreference(PREF_CLIENT_ID);
    mClientNamePref = (EditTextPreference) screen.findPreference(PREF_CLIENT_NAME);

    mSecuityPinPref = (PasswordPreference) screen.findPreference(PREF_SECURITY_PIN);
    mSecurityTrustedHotspotsPref = screen.findPreference(PREF_SECURITY_TRUSTED_HOTSPOTS_ENABLED);
    final Intent intentTrustedHotspots = new Intent(getBaseContext(), TrustedHotspotsActivity.class);
    mSecurityTrustedHotspotsPref.setIntent(intentTrustedHotspots);

    mClientSettings = new ClientSettings(getBaseContext());
    mClientSettings.addOnClientChangeListener(this);
  }

  @Override
  protected void onResume() {
    super.onResume();

    // clearing preferences to avoid conflicts
    clearPreferences();
    // starting listen for global settings changes
    mClientSettings.start();
    onClientChanged(mClientSettings.getClient());
    // starting listen for preferences changes
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    prefs.registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  protected void onPause() {
    super.onPause();

    // finishing listen for global settings changes
    mClientSettings.stop();
    // finishing listen for preferences changes
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    prefs.unregisterOnSharedPreferenceChangeListener(this);
    // clearing preferences to avoid conflicts
    clearPreferences();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (PREF_CLIENT_NAME.equals(key)) {
      mClientSettings.setName(sharedPreferences.getString(PREF_CLIENT_NAME, null)).commit();
    } else if (PREF_SECURITY_PIN.equals(key)) {
      final String pin = sharedPreferences.getString(PREF_SECURITY_PIN, null);
      mClientSettings.setPin(pin).commit();
    }

    onClientChanged(mClientSettings.getClient());
  }

  @Override
  public void onClientChanged(final Client client) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mClientIdPref.setSummary(client.getId());
        mClientNamePref.setSummary(client.getName());
        mClientNamePref.setText(client.getName());
        mSecuityPinPref.setSummary(client.getUsePin() ? R.string.pref_security_pin_summary_set : R.string.pref_security_pin_summary_not_set);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
          final SwitchPreference pref = (SwitchPreference) mSecurityTrustedHotspotsPref;
          pref.setChecked(prefs.getBoolean(PREF_SECURITY_TRUSTED_HOTSPOTS_ENABLED, false));
        } else {
          final CheckBoxPreference pref = (CheckBoxPreference) mSecurityTrustedHotspotsPref;
          pref.setChecked(prefs.getBoolean(PREF_SECURITY_TRUSTED_HOTSPOTS_ENABLED, false));
        }
      }
    });
  }

  private void clearPreferences() {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    final SharedPreferences.Editor prefsEraser = prefs.edit();
    // removing client preferences only
    prefsEraser.remove(PREF_CLIENT_ID).remove(PREF_CLIENT_NAME).remove(PREF_SECURITY_PIN);
    SharedPreferencesHelper.apply(prefsEraser);
  }
}