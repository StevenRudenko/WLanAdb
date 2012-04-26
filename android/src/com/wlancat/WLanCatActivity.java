package com.wlancat;

import com.wlancat.service.WLanCatService;
import com.wlancat.service.WLanServiceApi;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

public class WLanCatActivity extends Activity {
  private static final String TAG = WLanCatActivity.class.getSimpleName();

  private WLanServiceApi mServiceApi;

  private TextView viewMessage;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    viewMessage = (TextView) findViewById(android.R.id.message);
  }

  @Override
  protected void onResume() {
    super.onResume();

    Log.v(TAG, "Starting service (if it was not started before)...");
    final Intent intent = new Intent(WLanCatService.class.getName());
    // start the service explicitly.
    // otherwise it will only run while the IPC connection is up.
    startService(intent);
    bindService(intent, serviceConnection, 0);
  }

  @Override
  protected void onPause() {
    super.onPause();

    unbindService(serviceConnection);
    Log.v(TAG, "Activity paused");
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
          viewMessage.setText(log.toString());
        } catch (RemoteException e) {
          Log.e(TAG, "Fail to call service API", e);
        }
      }
    });
  }

  private ServiceConnection serviceConnection = new ServiceConnection() {
    public void onServiceConnected(ComponentName name, IBinder service) {
      Log.i(TAG, "Service connection established");
      // that's how we get the client side of the IPC connection
      mServiceApi = WLanServiceApi.Stub.asInterface(service);

      updateInfo();
    }

    public void onServiceDisconnected(ComponentName name) {
      Log.i(TAG, "Service connection closed");
    }
  };
}