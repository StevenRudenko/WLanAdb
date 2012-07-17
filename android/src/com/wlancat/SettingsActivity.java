package com.wlancat;

import com.wlancat.compat.SharedPreferencesApply;
import com.wlancat.data.ClientProto.Client;
import com.wlancat.data.ClientSettings.OnClientChangeListener;
import com.wlancat.data.ClientSettings;
import com.wlancat.ui.prefs.PasswordPreference;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnClientChangeListener {

  private static final String PREF_CLIENT_ID = "client_id";
  private static final String PREF_CLIENT_NAME = "client_name";
  private static final String PREF_SECURITY_PIN = "security_pin";

  private ClientSettings mClientSettings;

  private EditTextPreference mClientIdPref;
  private EditTextPreference mClientNamePref;

  private PasswordPreference mSecuityPinPref;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preference_settings);

    final PreferenceScreen screen = getPreferenceScreen();
    mClientIdPref = (EditTextPreference) screen.findPreference(PREF_CLIENT_ID);
    mClientNamePref = (EditTextPreference) screen.findPreference(PREF_CLIENT_NAME);

    mSecuityPinPref = (PasswordPreference) screen.findPreference(PREF_SECURITY_PIN);

    mClientSettings = new ClientSettings(this);
    mClientSettings.addOnClientChangeListener(this);
  }

  @Override
  protected void onResume() {
    super.onResume();

    // clearing preferences to avoid conflicts
    clearPreferences();
    // starting listen for global settings changes
    mClientSettings.start();
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
  public void onClientChanged(Client client) {
    mClientIdPref.setSummary(client.getId());
    mClientNamePref.setSummary(client.getName());
    mClientNamePref.setText(client.getName());
    mSecuityPinPref.setSummary(client.getUsePin() ? R.string.pref_security_pin_summary_set : R.string.pref_security_pin_summary_not_set);
  }

  private void clearPreferences() {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    final SharedPreferences.Editor prefsEraser = prefs.edit();
    // removing client preferences only
    prefsEraser.remove(PREF_CLIENT_ID).remove(PREF_CLIENT_NAME).remove(PREF_SECURITY_PIN);
    SharedPreferencesApply.apply(prefsEraser);
  }
}