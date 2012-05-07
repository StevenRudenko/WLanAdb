package com.wlancat.data;

import java.net.InetAddress;

import net.sf.signalslot_apt.annotations.signal;
import net.sf.signalslot_apt.annotations.signalslot;

import com.wlancat.compat.SharedPreferencesApply;
import com.wlancat.data.ClientProto.Client;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;

@signalslot(force_concrete=true)
public abstract class ClientSettings implements OnSharedPreferenceChangeListener {

  private static final String PREF_FILENAME = "client";
  private static final String PREF_CLIENT_ID = "client_id";
  private static final String PREF_CLIENT_IP = "client_ip";
  private static final String PREF_CLIENT_PORT = "client_port";
  private static final String PREF_CLIENT_NAME = "client_name";
  private static final String PREF_CLIENT_PIN = "client_pin";

  private final SharedPreferences mPrefs;
  private Client mClient;

  public ClientSettings(Context context) {
    mPrefs = context.getSharedPreferences(PREF_FILENAME, Context.MODE_PRIVATE);
    readClient();
  }

  @signal
  public abstract void onClientChanged(Client client);

  public void start() {
    mPrefs.registerOnSharedPreferenceChangeListener(this);
  }

  public void stop() {
    mPrefs.registerOnSharedPreferenceChangeListener(this);
  }

  public Client getClient() {
    return mClient;
  }

  public boolean hasPin() {
    return mPrefs.getString(PREF_CLIENT_PIN, null) != null;
  }

  public boolean checkPin(String pin) {
    final String checkPin = mPrefs.getString(PREF_CLIENT_PIN, null);
    if (pin == null)
      return true;
    return checkPin.equals(pin);
  }

  public void setId(String id) {
    final SharedPreferences.Editor editor = mPrefs.edit()
        .putString(PREF_CLIENT_ID, id);
    SharedPreferencesApply.apply(editor);
  }

  public void setIp(InetAddress address) {
    final SharedPreferences.Editor editor = mPrefs.edit()
        .putString(PREF_CLIENT_IP, address.getHostAddress());
    SharedPreferencesApply.apply(editor);
  }

  public void setPort(int port) {
    final SharedPreferences.Editor editor = mPrefs.edit()
        .putInt(PREF_CLIENT_PORT, port);
    SharedPreferencesApply.apply(editor);
  }

  public Client readClient() {
    final String id = mPrefs.getString(PREF_CLIENT_ID, Build.MODEL);
    final String ip = mPrefs.getString(PREF_CLIENT_IP, "");
    final int port = mPrefs.getInt(PREF_CLIENT_PORT, -1);
    final String name = mPrefs.getString(PREF_CLIENT_NAME, Build.MODEL);

    final Client.Builder builder = mClient == null ? Client.newBuilder() : mClient.toBuilder();
    mClient = builder
        .setId(id)
        .setIp(ip)
        .setPort(port)
        .setName(name)
        .setUsePin(hasPin())
        .build();
    return mClient;
  }

  public void saveClient() {
    final SharedPreferences.Editor editor = mPrefs.edit()
        .putString(PREF_CLIENT_ID, mClient.getId())
        .putString(PREF_CLIENT_IP, mClient.getIp())
        .putInt(PREF_CLIENT_PORT, mClient.getPort())
        .putString(PREF_CLIENT_NAME, mClient.getName());
    SharedPreferencesApply.apply(editor);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
      String key) {

    final Client.Builder builder = mClient == null ? Client.newBuilder() : mClient.toBuilder();
    if (PREF_CLIENT_ID.equals(key)) {
      final String id = sharedPreferences.getString(PREF_CLIENT_ID, Build.MODEL);
      mClient = builder.setId(id).build();
    } else if (PREF_CLIENT_IP.equals(key)) {
      final String ip = sharedPreferences.getString(PREF_CLIENT_IP, "");
      mClient = builder.setIp(ip).build();
    } else if (PREF_CLIENT_PORT.equals(key)) {
      final int port = sharedPreferences.getInt(PREF_CLIENT_PORT, -1);
      mClient = builder.setPort(port).build();
    } else if (PREF_CLIENT_NAME.equals(key)) {
      final String name = sharedPreferences.getString(PREF_CLIENT_NAME, null);
      mClient = builder.setName(name).build();
    } else if (PREF_CLIENT_PIN.equals(key)) {
      mClient = builder.setUsePin(hasPin()).build();
    } else
      return;

    onClientChanged(mClient);
  }
}
