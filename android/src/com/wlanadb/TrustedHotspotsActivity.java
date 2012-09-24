package com.wlanadb;

import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.wlanadb.actionbar.ActionBarActivity;
import com.wlanadb.compat.SharedPreferencesHelper;
import com.wlanadb.fragment.TrustedHotspotsFragment;
import com.wlanadb.ui.prefs.Preferences;

public class TrustedHotspotsActivity extends ActionBarActivity {

  private TrustedHotspotsFragment mFragment;
  private SharedPreferences mPrefs;
  private SharedPreferences mTrustedSSIDsPrefs;

  private CompoundButton viewToggleSwitch;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_trusted_hotspots);

    mPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    mTrustedSSIDsPrefs = getSharedPreferences(Preferences.PREF_SECURITY_TRUSTED_HOTSPOTS_SET, Context.MODE_PRIVATE);
    mFragment = (TrustedHotspotsFragment) getSupportFragmentManager().findFragmentById(R.id.content);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
      viewToggleSwitch = inflateToggleSwitcher();
      getActionBarHelper().setCustomView(viewToggleSwitch);
      updateToggleSwitch();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      final MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.menu_trusted_hotspots, menu);
      viewToggleSwitch = inflateToggleSwitcher();
      menu.findItem(R.id.menu_apply).setActionView(viewToggleSwitch);
      updateToggleSwitch();
    }
    return true;
  }

  @Override
  protected void onResume() {
    updateToggleSwitch();

    final Map<String, ?> values = mTrustedSSIDsPrefs.getAll();
    mFragment.setTrustedSSIDs(values.keySet());

    super.onResume();
  }

  @Override
  protected void onPause() {
    final SharedPreferences.Editor toggleSwitchEditor = mPrefs.edit();
    toggleSwitchEditor.putBoolean(Preferences.PREF_SECURITY_TRUSTED_HOTSPOTS_ENABLED, viewToggleSwitch.isChecked());
    SharedPreferencesHelper.apply(toggleSwitchEditor);

    final SharedPreferences.Editor trustedSSIDsEditor = mTrustedSSIDsPrefs.edit();
    trustedSSIDsEditor.clear();
    final Set<String> trustedSSIDs = mFragment.getTrustedSSIDs();
    for (String value : trustedSSIDs) {
      trustedSSIDsEditor.putBoolean(value, true);
    }
    SharedPreferencesHelper.apply(trustedSSIDsEditor);

    super.onPause();
  }

  private void updateToggleSwitch() {
    final boolean checked = mPrefs.getBoolean(Preferences.PREF_SECURITY_TRUSTED_HOTSPOTS_ENABLED, false);
    onToggleSwitcherChanged(checked);
  }

  private void onToggleSwitcherChanged(boolean isChecked) {
    if (viewToggleSwitch == null)
      return;

    viewToggleSwitch.setChecked(isChecked);
    mFragment.setEnabled(isChecked);
  }

  private CompoundButton inflateToggleSwitcher() {
    final LayoutInflater inflater = LayoutInflater.from(getBaseContext());
    final CompoundButton viewToggle = (CompoundButton) inflater.inflate(R.layout.actionbar_toggle, null);
    final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
    viewToggle.setLayoutParams(layoutParams);
    viewToggle.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onToggleSwitcherChanged(viewToggle.isChecked());
      }
    });
    return viewToggle;
  }
}
