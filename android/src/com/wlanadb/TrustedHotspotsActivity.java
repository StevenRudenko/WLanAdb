package com.wlanadb;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.wlanadb.actionbar.ActionBarActivity;

public class TrustedHotspotsActivity extends ActionBarActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_trusted_hotspots);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_trusted_hotspots, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    final int menuId = item.getItemId();
    switch (menuId) {
    case R.id.menu_apply:
      setResult(RESULT_OK);
      finish();
      break;

    default:
      break;
    }
    return super.onOptionsItemSelected(item);
  }
}
