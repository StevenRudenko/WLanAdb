package com.wlanadb.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.wlanadb.config.MyConfig;
import com.wlanadb.data.ClientProto.Client;
import com.wlanadb.utils.AndroidUtils;
import com.wlanadb.utils.FileWatchdog;
import com.wlanadb.utils.IOUtilities;
import com.wlanadb.utils.WeakHashSet;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

public class Settings extends FileWatchdog {
  private static final String TAG = Settings.class.getSimpleName();
  private static final boolean DEBUG = MyConfig.DEBUG && true;

  public interface OnSettingsChangeListener {
    public void onSettingsChanged();
  }

  private static final String FILENAME = "settings.bin";

  private final String mDeviceId;

  private Client mClient;
  private String mPin;

  private boolean isTrustedHotspotsEnabled = false;
  private Set<String> mTrustedHotspots = new HashSet<String>();

  private Set<OnSettingsChangeListener> mListeners = new WeakHashSet<OnSettingsChangeListener>();

  public Settings(Context context) {
    super(context.getFileStreamPath(FILENAME));

    mDeviceId = generateSerial(context);
    refresh();
  }

  public void addOnClientChangeListener(OnSettingsChangeListener listener) {
    synchronized (mListeners) {
      mListeners.add(listener);
      if (mClient != null)
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
    return mClient;
  }

  public String getPin() {
    return mPin;
  }

  public boolean isTrustedHotspotsEnabled() {
    return isTrustedHotspotsEnabled;
  }

  public boolean isThustedHotspot(String ssid) {
    return mTrustedHotspots.contains(ssid);
  }

  public Set<String> getTrustedHotspots() {
    return mTrustedHotspots;
  }

  public Settings setPin(String pin) {
    if (DEBUG)
      Log.v(TAG, "setPin: " + pin);

    final boolean hasPin = !TextUtils.isEmpty(pin);
    mPin = hasPin ? pin : null;
    mClient = mClient.toBuilder().setUsePin(hasPin).build();

    return this;
  }

  public boolean hasPin() {
    return !TextUtils.isEmpty(mPin);
  }

  public boolean checkPin(String pin) {
    if (DEBUG)
      Log.v(TAG, "checkPin: " + pin);

    if (TextUtils.isEmpty(mPin))
      return true;

    if (DEBUG)
      Log.d(TAG, "Check PIN: " + mPin + " <> " + pin);
    return mPin.equals(pin);
  }

  public Settings setIp(InetAddress address) {
    if (DEBUG)
      Log.v(TAG, "setIp: " + address.getHostAddress());

    mClient = mClient.toBuilder().setIp(address.getHostAddress()).build();
    return this;
  }

  public Settings setPort(int port) {
    if (DEBUG)
      Log.v(TAG, "setPort: " + port);

    mClient = mClient.toBuilder().setPort(port).build();
    return this;
  }

  public Settings setName(String name) {
    if (DEBUG)
      Log.v(TAG, "setName: " + name);

    if (TextUtils.isEmpty(name))
      name = Build.MODEL;
    mClient = mClient.toBuilder().setName(name).build();
    return this;
  }

  public Settings setTrustedHotspotsEnabled(boolean enabled) {
    isTrustedHotspotsEnabled = enabled;
    return this;
  }

  public Settings setTrustedHotspots(Set<String> hotspots) {
    mTrustedHotspots.clear();
    mTrustedHotspots.addAll(hotspots);
    return this;
  }

  public Settings commit() {
    saveToFile();
    return this;
  }

  public Settings refresh() {
    readFromFile();
    return this;
  }

  @Override
  public boolean onFileChanged() {
    readFromFile();
    return true;
  }

  private String generateSerial(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
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

  private void readFromFile() {
    if (!getFile().exists()) {
      mClient = Client.newBuilder()
          .setId(mDeviceId)
          .setName(Build.MODEL)
          .setModel(Build.MODEL)
          .setFirmware(Build.VERSION.RELEASE)
          .build();
      mPin = null;
      return;
    }

    if (DEBUG)
      Log.d(TAG, "Reading client data from file...");

    ObjectInputStream in = null;
    try {
      in = new ObjectInputStream(new FileInputStream(getFile()));
      final int clientSize = in.readInt();
      final byte[] clientBytes = new byte[clientSize];
      in.read(clientBytes, 0, clientSize);
      final String pin = (String) in.readObject();
      final boolean hasPin = !TextUtils.isEmpty(pin);

      final boolean trustedHotspotsEnabled = in.readBoolean();
      final int trustedHotspotsCount = in.readInt();
      final Set<String> trustedHotspots = new HashSet<String>();
      for (int i=0; i<trustedHotspotsCount; ++i) {
        trustedHotspots.add((String) in.readObject());
      }

      isTrustedHotspotsEnabled = trustedHotspotsEnabled;
      mTrustedHotspots.clear();
      mTrustedHotspots.addAll(trustedHotspots);

      mPin = hasPin ? pin : null;
      mClient = Client.newBuilder()
          .mergeFrom(clientBytes)
          .setUsePin(hasPin)
          .build();

      onSettingsChanged();
    } catch (FileNotFoundException e) {
      if (DEBUG)
        Log.e(TAG, "Settings file not found", e);
    } catch (IOException e) {
      if (DEBUG)
        Log.e(TAG, "Fail to read settings file", e);
    } catch (ClassNotFoundException e) {
      if (DEBUG)
        Log.e(TAG, "Fail to read string from setting file", e);
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

    lockFile();
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(new FileOutputStream(file, false));
      final byte[] clientBytes = mClient.toByteArray();
      final int clientSize = clientBytes.length;
      out.writeInt(clientSize);
      out.write(clientBytes);
      out.writeObject(mPin);
      out.writeBoolean(isTrustedHotspotsEnabled);
      out.writeInt(mTrustedHotspots.size());
      for (String hotspot : mTrustedHotspots) {
        out.writeObject(hotspot);
      }
    } catch (FileNotFoundException e) {
      if (DEBUG)
        Log.e(TAG, "Settings file not found", e);
    } catch (IOException e) {
      if (DEBUG)
        Log.e(TAG, "Fail to read settings file", e);
    } finally {
      IOUtilities.closeStream(out);
      unlockFile();
    }
  }
}
