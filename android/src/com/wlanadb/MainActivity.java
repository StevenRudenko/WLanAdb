package com.wlanadb;

import com.wlanadb.actionbar.ActionBarActivity;
import com.wlanadb.config.MyConfig;
import com.wlanadb.service.ConnectionsStatusReciever;
import com.wlanadb.service.WLanAdbService;
import com.wlancat.service.WLanServiceApi;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {
  private static final String TAG = MainActivity.class.getSimpleName();
  private static final boolean DEBUG = MyConfig.DEBUG && true;

  private WLanServiceApi mServiceApi;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (DEBUG)
      Log.v(TAG, "Starting service (if it was not started before)...");
    final Intent intent = new Intent(WLanAdbService.class.getName());
    // start the service explicitly.
    // otherwise it will only run while the IPC connection is up.
    startService(intent);
    bindService(intent, serviceConnection, 0);
  }

  @Override
  protected void onPause() {
    super.onPause();

    connectionsCountReciever.unregister(this);

    unbindService(serviceConnection);
    if (DEBUG)
      Log.v(TAG, "Activity paused");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    final int menuId = item.getItemId();
    switch (menuId) {
    case R.id.menu_settings:
      final Intent settingIntent = new Intent(this, SettingsActivity.class);
      startActivity(settingIntent);
      break;

    default:
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void updateInfo() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        try {
          final StringBuilder log = new StringBuilder();
          log.append("Local IP address: ");
          log.append(mServiceApi.getAddress());
          log.append("\nCreated server on port: ");
          log.append(mServiceApi.getPort());
          log.append("\nConnections count: ");
          log.append(mServiceApi.getConnectionsCount());
        } catch (RemoteException e) {
          if (DEBUG)
            Log.e(TAG, "Fail to call service API", e);
        }
      }
    });
  }

  private ServiceConnection serviceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      if (DEBUG)
        Log.i(TAG, "Service connection established");
      // that's how we get the client side of the IPC connection
      mServiceApi = WLanServiceApi.Stub.asInterface(service);

      try {
        if (!mServiceApi.checkTrustedHotspots()) {
          unbindService(serviceConnection);
          connectionsCountReciever.unregister(getBaseContext());
        } else {
          connectionsCountReciever.register(getBaseContext());
          updateInfo();
        }
      } catch (RemoteException e) {
        if (DEBUG)
          Log.e(TAG, "Fail to execute WiFi hotspot check.", e);
        unbindService(serviceConnection);
        connectionsCountReciever.unregister(getBaseContext());
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      if (DEBUG)
        Log.i(TAG, "Service connection closed");
    }
  };

  private ConnectionsStatusReciever connectionsCountReciever = new ConnectionsStatusReciever() {

    @Override
    public void onConnectionCountChanged(int connectionsCount) {
      updateInfo();
    }
  };
}