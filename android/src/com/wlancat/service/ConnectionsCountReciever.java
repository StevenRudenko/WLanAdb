package com.wlancat.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class ConnectionsCountReciever extends BroadcastReceiver {
  private static final String TAG = ConnectionsCountReciever.class.getSimpleName();

  public static final String ACTION_CONNECTIONS_COUNT = TAG+".CONNECTIONS_COUNT";

  public static final String EXTRA_CONNECTIONS_COUNT = TAG+".extra.COUNT";

  public abstract void onConnectionCountChanged(int connectionsCount);

  public void register(Context context) {
    final IntentFilter filter = new IntentFilter(ACTION_CONNECTIONS_COUNT);
    context.registerReceiver(this, filter);
  }

  public void unregister(Context context) {
    context.unregisterReceiver(this);
  }

  @Override
  public final void onReceive(Context context, Intent intent) {
    if (!intent.getAction().equals(ACTION_CONNECTIONS_COUNT))
      return;

    final int connectionsCount = intent.getIntExtra(EXTRA_CONNECTIONS_COUNT, 0);
    onConnectionCountChanged(connectionsCount);
  }

}
