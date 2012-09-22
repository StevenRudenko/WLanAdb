package com.wlanadb;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.wlanadb.actionbar.ActionBarActivity;
import com.wlanadb.fragment.TrustedHotspotsFragment;

public class TrustedHotspotsActivity extends ActionBarActivity {

  private TrustedHotspotsFragment mFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_trusted_hotspots);

    mFragment = (TrustedHotspotsFragment) getSupportFragmentManager().findFragmentById(R.id.content);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
      final View v = inflateToggleSwitcher();
      getActionBarHelper().setCustomView(v);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      final MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.menu_trusted_hotspots, menu);
      final View v = inflateToggleSwitcher();
      menu.findItem(R.id.menu_apply).setActionView(v);
    }
    return true;
  }

  private void onToggleSwitcherChanged(boolean isChecked) {
    mFragment.setEnabled(isChecked);
  }

  private View inflateToggleSwitcher() {
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
