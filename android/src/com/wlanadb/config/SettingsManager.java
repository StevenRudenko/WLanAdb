package com.wlanadb.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.wlanadb.data.ClientProto.Client;
import com.wlanadb.data.SettingsProto.Settings;
import com.wlanadb.utils.AndroidUtils;
import com.wlanadb.utils.FileWatchdog;
import com.wlanadb.utils.IOUtilities;
import com.wlanadb.utils.WeakHashSet;

public class SettingsManager extends FileWatchdog {
  private static final String TAG = SettingsManager.class.getSimpleName();
  private static final boolean DEBUG = MyConfig.DEBUG && true;

  public interface OnSettingsChangeListener {
    public void onSettingsChanged();
  }

  private static final String FILENAME = "settings.proto.bin";

  private final String mDeviceId;

  private Settings mSettings;

  private Set<OnSettingsChangeListener> mListeners = new WeakHashSet<OnSettingsChangeListener>();

  public SettingsManager(Context context) {
    super(context.getFileStreamPath(FILENAME));

    mDeviceId = generateSerial(context);
    refresh();
  }

  public void addOnClientChangeListener(OnSettingsChangeListener listener) {
    synchronized (mListeners) {
      mListeners.add(listener);
      if (mSettings != null)
        listener.onSettingsChanged();
    }
  }

  public void removeOnClientChangeListener(OnSettingsChangeListener listener) {
    synchronized (mListeners) {
      mListeners.remove(listener);
    }
  }

  public void onSettingsChanged() {
    synchronized (mListeners) {
      for (OnSettingsChangeListener listener : mListeners) {
        listener.onSettingsChanged();
      }
    }
  }

  public Client getClient() {
    return mSettings.getClient();
  }

  public String getPin() {
    return mSettings.getPin();
  }

  public boolean isTrustedHotspotsEnabled() {
    return mSettings.getTrustedHotspotsEnabled();
  }

  public boolean isThustedHotspot(String ssid) {
    final int count = mSettings.getTrustedHotspotsCount();
    for (int i=0; i<count; ++i) {
      if (mSettings.getTrustedHotspots(i).equals(ssid))
        return true;
    }
    return false;
  }

  public Set<String> getTrustedHotspots() {
    final int count = mSettings.getTrustedHotspotsCount();
    final Set<String> result = new HashSet<String>();
    for (int i=0; i<count; ++i) {
      result.add(mSettings.getTrustedHotspots(i));
    }
    return result;
  }

  public boolean getAskToInstall() {
    return mSettings.getAskToInstall();
  }

  public boolean getWifiLockEnabled() {
    return mSettings.getWifiLockEnabled();
  }

  public SettingsManager setPin(String pin) {
    if (DEBUG)
      Log.v(TAG, "setPin: " + pin);

    final boolean hasPin = !TextUtils.isEmpty(pin);
    final Settings.Builder builder = mSettings.toBuilder()
        .setClient(mSettings.getClient().toBuilder().setUsePin(hasPin).build());

    if (pin == null)
      builder.clearPin();
    else
      builder.setPin(pin);

    mSettings = builder.build();
    return this;
  }

  public boolean hasPin() {
    return !TextUtils.isEmpty(mSettings.getPin());
  }

  public boolean checkPin(String pin) {
    if (DEBUG)
      Log.v(TAG, "checkPin: " + pin);

    final String clientPin = mSettings.getPin();
    if (TextUtils.isEmpty(clientPin))
      return true;

    if (DEBUG)
      Log.d(TAG, "Check PIN: " + clientPin + " <> " + pin);
    return clientPin.equals(pin);
  }

  public SettingsManager setIp(InetAddress address) {
    if (DEBUG)
      Log.v(TAG, "setIp: " + address.getHostAddress());

    mSettings = mSettings.toBuilder()
        .setClient(mSettings.getClient().toBuilder().setIp(address.getHostAddress()).build())
        .build();
    return this;
  }

  public SettingsManager setPort(int port) {
    if (DEBUG)
      Log.v(TAG, "setPort: " + port);

    mSettings = mSettings.toBuilder()
        .setClient(mSettings.getClient().toBuilder().setPort(port).build())
        .build();
    return this;
  }

  public SettingsManager setName(String name) {
    if (DEBUG)
      Log.v(TAG, "setName: " + name);

    if (TextUtils.isEmpty(name))
      name = Build.MODEL;

    mSettings = mSettings.toBuilder()
        .setClient(mSettings.getClient().toBuilder().setName(name).build())
        .build();
    return this;
  }

  public SettingsManager setTrustedHotspotsEnabled(boolean enabled) {
    mSettings = mSettings.toBuilder()
        .setTrustedHotspotsEnabled(enabled)
        .build();
    return this;
  }

  public SettingsManager setTrustedHotspots(Set<String> hotspots) {
    mSettings = mSettings.toBuilder()
        .clearTrustedHotspots()
        .addAllTrustedHotspots(hotspots)
        .build();
    return this;
  }

  public SettingsManager setAskToInstall(boolean enabled) {
    mSettings = mSettings.toBuilder()
        .setAskToInstall(enabled)
        .build();
    return this;
  }

  public SettingsManager setWifiLockEnabled(boolean enabled) {
    mSettings = mSettings.toBuilder()
        .setWifiLockEnabled(enabled)
        .build();
    return this;
  }

  public SettingsManager commit() {
    saveToFile();
    return this;
  }

  public SettingsManager refresh() {
    readFromFile();
    return this;
  }

  @Override
  public boolean onFileChanged() {
    readFromFile();
    return true;
  }

  private void readFromFile() {
    if (!getFile().exists()) {
      final Client client = Client.newBuilder()
          .setId(mDeviceId)
          .setName(Build.MODEL)
          .setModel(Build.MODEL)
          .setFirmware(Build.VERSION.RELEASE)
          .build();
      mSettings = Settings.newBuilder()
          .setClient(client)
          .build();
      return;
    }

    if (DEBUG)
      Log.d(TAG, "Reading client data from file...");

    InputStream in = null;
    try {
      in = new FileInputStream(getFile());
      mSettings = Settings.parseFrom(in);

      onSettingsChanged();
    } catch (FileNotFoundException e) {
      if (DEBUG)
        Log.e(TAG, "Settings file not found", e);
    } catch (IOException e) {
      if (DEBUG)
        Log.e(TAG, "Fail to read settings file", e);
    } finally {
      IOUtilities.closeStream(in);
    }
  }

  private void saveToFile() {
    final File file = getFile();
    if (!file.exists()) {
      file.getParentFile().mkdirs();
    }

    if (DEBUG)
      Log.d(TAG, "Writing client data to file...");

    // uncomment this is there will be separate process use needed
    //lockFile();
    OutputStream out = null;
    try {
      out = new FileOutputStream(file, false);
      mSettings.writeTo(out);
      out.flush();
    } catch (FileNotFoundException e) {
      if (DEBUG)
        Log.e(TAG, "Settings file not found", e);
    } catch (IOException e) {
      if (DEBUG)
        Log.e(TAG, "Fail to write settings file", e);
    } finally {
      IOUtilities.closeStream(out);
      // uncomment this is there will be separate process use needed
      //unlockFile();
    }
  }

  private static String generateSerial(Context context) {
    if (Build.VERSION.SDK_INT >= 9) {
      if (!Build.SERIAL.toLowerCase().equals("unknown"))
        return Build.SERIAL;
    }

    final String secureId = AndroidUtils.getAndroidId(context);
    if (secureId == null || secureId.toLowerCase().equals("unknown")) {
      return UUID.randomUUID().toString().toUpperCase();
    } else {
      return secureId.toUpperCase();
    }
  }
}
