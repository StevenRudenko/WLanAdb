package com.wlancat.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.wlancat.config.MyConfig;
import com.wlancat.data.ClientProto.Client;
import com.wlancat.utils.AndroidUtils;
import com.wlancat.utils.IOUtilities;

import android.content.Context;
import android.os.Build;
import android.os.FileObserver;
import android.text.TextUtils;
import android.util.Log;

public class ClientSettings {
  private static final String TAG = ClientSettings.class.getSimpleName();
  private static final boolean DEBUG = MyConfig.DEBUG && true;

  public interface OnClientChangeListener {
    public void onClientChanged(Client client);
  }

  private static final String FILENAME = "ClientSettings.bin";

  private final String mDeviceId;
  private final File mSettingsFile;

  /**
   * The count of how many known (handled by SettingsProvider) 
   * database mutations are currently being handled.  Used by
   * mObserverInstance to not reload the data when it's ourselves
   * modifying it.
   */
  private final AtomicInteger mKnownMutationsInFlight = new AtomicInteger(0);
  private final SettingsFileObserver mObserverInstance;

  private Client mClient;
  private String mPin;

  private Set<OnClientChangeListener> mListeners = Collections.newSetFromMap(
      new WeakHashMap<OnClientChangeListener, Boolean>());

  public ClientSettings(Context context) {
    final String secureId = AndroidUtils.getAndroidId(context);
    if (secureId == null) {
      mDeviceId = UUID.randomUUID().toString();
    } else {
      mDeviceId = secureId.toUpperCase();
    }

    mSettingsFile = context.getFileStreamPath(FILENAME);

    refresh();

    mObserverInstance = new SettingsFileObserver(mSettingsFile.getAbsolutePath());
  }

  public void addOnClientChangeListener(OnClientChangeListener listener) {
    synchronized (mListeners) {
      mListeners.add(listener);
      listener.onClientChanged(mClient);
    }
  }

  public void removeOnClientChangeListener(OnClientChangeListener listener) {
    synchronized (mListeners) {
      mListeners.remove(listener);
    }
  }

  public void onClientChanged(Client client) {
    synchronized (mListeners) {
      for (OnClientChangeListener listener : mListeners) {
        listener.onClientChanged(client);
      }
    }
  }

  public void start() {
    if (DEBUG)
      Log.v(TAG, "starting...");

    mObserverInstance.startWatching();
  }

  public void stop() {
    if (DEBUG)
      Log.v(TAG, "stopping...");

    mObserverInstance.stopWatching();
  }

  public Client getClient() {
    return mClient;
  }

  public String getPin() {
    return mPin;
  }

  public ClientSettings setPin(String pin) {
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

  public ClientSettings setIp(InetAddress address) {
    if (DEBUG)
      Log.v(TAG, "setIp: " + address.getHostAddress());

    mClient = mClient.toBuilder().setIp(address.getHostAddress()).build();
    return this;
  }

  public ClientSettings setPort(int port) {
    if (DEBUG)
      Log.v(TAG, "setPort: " + port);

    mClient = mClient.toBuilder().setPort(port).build();
    return this;
  }

  public ClientSettings setName(String name) {
    if (DEBUG)
      Log.v(TAG, "setName: " + name);

    mClient = mClient.toBuilder().setName(name).build();
    return this;
  }

  public ClientSettings commit() {
    saveToFile();
    return this;
  }

  public ClientSettings refresh() {
    readFromFile();
    return this;
  }

  private void readFromFile() {
    if (!mSettingsFile.exists()) {
      mClient = Client.newBuilder()
          .setId(mDeviceId)
          .setName(Build.MODEL)
          .build();
      mPin = null;
      return;
    }

    if (DEBUG)
      Log.d(TAG, "Reading client data from file...");

    ObjectInputStream in = null;
    try {
      in = new ObjectInputStream(new FileInputStream(mSettingsFile));
      final int clientSize = in.readInt();
      final byte[] clientBytes = new byte[clientSize];
      in.read(clientBytes, 0, clientSize);
      final String pin = (String) in.readObject();
      final boolean hasPin = !TextUtils.isEmpty(pin);

      mPin = hasPin ? pin : null;
      mClient = Client.newBuilder()
          .mergeFrom(clientBytes)
          .setUsePin(hasPin)
          .build();

      onClientChanged(mClient);
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
    if (!mSettingsFile.exists()) {
      mSettingsFile.getParentFile().mkdirs();
    }

    if (DEBUG)
      Log.d(TAG, "Writing client data to file...");

    mKnownMutationsInFlight.incrementAndGet();
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(new FileOutputStream(mSettingsFile, false));
      final byte[] clientBytes = mClient.toByteArray();
      final int clientSize = clientBytes.length;
      out.writeInt(clientSize);
      out.write(clientBytes);
      out.writeObject(mPin);
    } catch (FileNotFoundException e) {
      if (DEBUG)
        Log.e(TAG, "Settings file not found", e);
    } catch (IOException e) {
      if (DEBUG)
        Log.e(TAG, "Fail to read settings file", e);
    } finally {
      IOUtilities.closeStream(out);
      mKnownMutationsInFlight.decrementAndGet();
    }
  }

  private class SettingsFileObserver extends FileObserver {
    private final AtomicBoolean mIsDirty = new AtomicBoolean(false);
    private final String mPath;

    public SettingsFileObserver(String path) {
      super(path, FileObserver.CLOSE_WRITE);
      mPath = path;
    }

    public void onEvent(int event, String path) {
      int modsInFlight = mKnownMutationsInFlight.get();
      if (modsInFlight > 0) {
        // our own modification.
        return;
      }

      if (DEBUG)
        Log.d(TAG, "external modification to " + mPath + "; event=" + event);

      if (!mIsDirty.compareAndSet(false, true)) {
        // already handled. (we get a few update events
        // during an data write)
        return;
      }

      if (DEBUG)
        Log.d(TAG, "updating our caches for " + mPath);

      readFromFile();
      mIsDirty.set(false);
    }
  }
}
