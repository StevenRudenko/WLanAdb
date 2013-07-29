package com.wlanadb;

import android.os.RemoteException;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.wlanadb.actionbar.ActionBarActivity;
import com.wlanadb.config.MyConfig;
import com.wlanadb.fragment.EnableWifiDialogFragment;
import com.wlanadb.service.ConnectionsStatusReciever;
import com.wlanadb.service.WLanAdbService;
import com.wlanadb.utils.WiFiUtils;
import com.wlancat.service.WLanServiceApi;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DEBUG = MyConfig.DEBUG && true;

    @SuppressWarnings("unused")
    private WLanServiceApi mServiceApi;

    private CheckBox viewConnections;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!WiFiUtils.isWifiEnabled(getBaseContext())) {
            EnableWifiDialogFragment.createDialog(this, EnableWifiDialogFragment.MSG_APP).show();
        }
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

        connectionsCountReciever.unregister(getBaseContext());

        unbindService(serviceConnection);
        if (DEBUG)
            Log.v(TAG, "Activity paused");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem menuTest = menu.findItem(R.id.menu_test_conn);
        viewConnections = (CheckBox) menuTest.getActionView();
        viewConnections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testConnection();
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int menuId = item.getItemId();
        switch (menuId) {
            case R.id.menu_settings:
                final Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingIntent);
                return true;
            case R.id.menu_test_conn:
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void testConnection() {
        if (mServiceApi == null) {
            Toast.makeText(getApplicationContext(), R.string.connection_status_undefined, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            mServiceApi.testConnection();
        } catch (RemoteException ignore) {
            Toast.makeText(getApplicationContext(), R.string.connection_status_undefined, Toast.LENGTH_SHORT).show();
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (DEBUG)
                Log.i(TAG, "Service connection established");
            // that's how we get the client side of the IPC connection
            mServiceApi = WLanServiceApi.Stub.asInterface(service);

            connectionsCountReciever.register(getBaseContext());

            testConnection();
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
            viewConnections.setText(Integer.toString(connectionsCount));
        }

        @Override
        public void onConnectionStatusChanged(int status) {
            switch (status) {
                case STATUS_UNDERFINED:
                    viewConnections.setSelected(false);
                    Toast.makeText(getApplicationContext(), R.string.connection_status_undefined, Toast.LENGTH_SHORT).show();
                    break;
                case STATUS_BLOCKED:
                    viewConnections.setSelected(true);
                    viewConnections.setChecked(false);
                    Toast.makeText(getApplicationContext(), R.string.connection_status_block, Toast.LENGTH_SHORT).show();
                    break;
                case STATUS_OK:
                    viewConnections.setSelected(true);
                    viewConnections.setChecked(true);
                    Toast.makeText(getApplicationContext(), R.string.connection_status_ok, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}