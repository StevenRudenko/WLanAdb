package com.wlanadb.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public abstract class ConnectionsStatusReciever extends BroadcastReceiver {
    private static final String TAG = ConnectionsStatusReciever.class.getSimpleName();

    public static final String ACTION_CONNECTIONS_CHANGED = TAG+".CONNECTIONS_CHANGED";

    public static final String EXTRA_CONNECTIONS_STATUS = TAG+".extra.STATUS";
    public static final String EXTRA_CONNECTIONS_COUNT = TAG+".extra.COUNT";


    public static final int STATUS_UNDERFINED = -1;
    public static final int STATUS_OK = 0;
    public static final int STATUS_BLOCKED = 1;

    public abstract void onConnectionCountChanged(int connectionsCount);

    public abstract void onConnectionStatusChanged(int status);

    private boolean isRegistered = false;

    public void register(Context context) {
        if (isRegistered)
            return;

        final IntentFilter filter = new IntentFilter(ACTION_CONNECTIONS_CHANGED);
        LocalBroadcastManager.getInstance(context).registerReceiver(this, filter);
        isRegistered = true;
    }

    public void unregister(Context context) {
        if (isRegistered) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
            isRegistered = false;
        }
    }

    @Override
    public final void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals(ACTION_CONNECTIONS_CHANGED))
            return;

        if (intent.hasExtra(EXTRA_CONNECTIONS_COUNT)) {
            final int count = intent.getIntExtra(EXTRA_CONNECTIONS_COUNT, 0);
            onConnectionCountChanged(count);
        }
        if (intent.hasExtra(EXTRA_CONNECTIONS_STATUS)) {
            final int status = intent.getIntExtra(EXTRA_CONNECTIONS_STATUS, STATUS_UNDERFINED);
            onConnectionStatusChanged(status);
        }
    }

}
