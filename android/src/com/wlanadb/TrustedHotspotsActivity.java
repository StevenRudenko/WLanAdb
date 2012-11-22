package com.wlanadb;

import java.util.Set;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.wlanadb.actionbar.ActionBarActivity;
import com.wlanadb.config.SettingsManager;
import com.wlanadb.fragment.TrustedHotspotsFragment;

public class TrustedHotspotsActivity extends ActionBarActivity {

  private SettingsManager mSettings;

  private TrustedHotspotsFragment mFragment;

  private CompoundButton viewToggleSwitch;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_trusted_hotspots);

    mFragment = (TrustedHotspotsFragment) getSupportFragmentManager().findFragmentById(R.id.content);

    mSettings = new SettingsManager(getBaseContext());
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
      viewToggleSwitch = inflateToggleSwitcher();
      getActionBarHelper().setCustomView(viewToggleSwitch);
      initToggleSwitch();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      final MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.menu_trusted_hotspots, menu);
      viewToggleSwitch = inflateToggleSwitcher();
      menu.findItem(R.id.menu_apply).setActionView(viewToggleSwitch);
      initToggleSwitch();
    }
    return true;
  }

  @Override
  protected void onResume() {
    initToggleSwitch();

    mFragment.setTrustedSSIDs(mSettings.getTrustedHotspots());

    super.onResume();
  }

  @Override
  protected void onPause() {
    final Set<String> trustedSSIDs = mFragment.getTrustedSSIDs();
    mSettings.setTrustedHotspots(trustedSSIDs).setTrustedHotspotsEnabled(viewToggleSwitch.isChecked()).commit();

    super.onPause();
  }

  private void initToggleSwitch() {
    onToggleSwitcherChanged(mSettings.isTrustedHotspotsEnabled());
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
    viewToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        onToggleSwitcherChanged(isChecked);
      }
    });
    return viewToggle;
  }
}
