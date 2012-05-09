package com.wlancat.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;

import net.sf.signalslot_apt.annotations.signal;
import net.sf.signalslot_apt.annotations.signalslot;

import com.wlancat.data.ClientProto.Client;
import com.wlancat.utils.AndroidUtils;
import com.wlancat.utils.HashHelper;
import com.wlancat.utils.IOUtilities;

import android.content.Context;
import android.os.Build;
import android.os.FileObserver;
import android.text.TextUtils;
import android.util.Log;

@signalslot(force_concrete=true)
public abstract class ClientSettings {
  private static final String TAG = ClientSettings.class.getSimpleName();

  private static final String FILENAME = "ClientSettings.bin";

  private final String mDeviceId;
  private final File mSettingsFile;
  private final FileObserver mSettingsFileObserver;

  private Client mClient;
  private String mPin;

  public ClientSettings(Context context) {
    mDeviceId = AndroidUtils.getAndroidId(context);
    mSettingsFile = context.getFileStreamPath(FILENAME);

    readFromFile();

    mSettingsFileObserver = new FileObserver(mSettingsFile.getAbsolutePath(), FileObserver.CLOSE_WRITE) {
      @Override
      public void onEvent(int event, String path) {
        readFromFile();
      }
    };
  }

  @signal
  public abstract void onClientChanged(Client client);

  public void start() {
    mSettingsFileObserver.startWatching();
  }

  public void stop() {
    mSettingsFileObserver.stopWatching();
  }

  public Client getClient() {
    return mClient;
  }

  public String getPin() {
    return mPin;
  }

  public void setPin(String pin) {
    final boolean hasPin = !TextUtils.isEmpty(pin);
    mPin = hasPin ? pin : null;
    mClient = mClient.toBuilder().setUsePin(hasPin).build();
    saveToFile();
  }

  public boolean hasPin() {
    return !TextUtils.isEmpty(mPin);
  }

  public boolean checkPin(String pin) {
    if (TextUtils.isEmpty(mPin))
      return true;
    final String hashPin = HashHelper.getHashString(pin);
    Log.d(TAG, "Check PIN: " + mPin + " <> " + hashPin);
    return mPin.equals(hashPin);
  }

  public void setIp(InetAddress address) {
    mClient = mClient.toBuilder().setIp(address.getHostAddress()).build();
    saveToFile();
  }

  public void setPort(int port) {
    mClient = mClient.toBuilder().setPort(port).build();
    saveToFile();
  }

  public void setName(String name) {
    mClient = mClient.toBuilder().setName(name).build();
    saveToFile();
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
      Log.e(TAG, "Settings file not found", e);
    } catch (IOException e) {
      Log.e(TAG, "Fail to read settings file", e);
    } catch (ClassNotFoundException e) {
      Log.e(TAG, "Fail to read string from setting file", e);
    } finally {
      IOUtilities.closeStream(in);
    }
  }

  private void saveToFile() {
    if (!mSettingsFile.exists()) {
      mSettingsFile.getParentFile().mkdirs();
    }

    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(new FileOutputStream(mSettingsFile, false));
      final byte[] clientBytes = mClient.toByteArray();
      final int clientSize = clientBytes.length;
      out.writeInt(clientSize);
      out.write(clientBytes);
      out.writeObject(mPin);
    } catch (FileNotFoundException e) {
      Log.e(TAG, "Settings file not found", e);
    } catch (IOException e) {
      Log.e(TAG, "Fail to read settings file", e);
    } finally {
      IOUtilities.closeStream(out);
    }
  }
}
