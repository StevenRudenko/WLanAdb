package com.wlanadb.utils;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.FileObserver;
import android.util.Log;

import com.wlanadb.config.MyConfig;

public abstract class FileWatchdog {
  private static final String TAG = FileWatchdog.class.getSimpleName();
  private static final boolean DEBUG = MyConfig.DEBUG && true;

  /**
   * The count of how many known (handled by SettingsProvider) 
   * database mutations are currently being handled.  Used by
   * mObserverInstance to not reload the data when it's ourselves
   * modifying it.
   */
  private final AtomicInteger mKnownMutationsInFlight = new AtomicInteger(0);
  private final WatchdogFileObserver mObserverInstance;

  private final File mWatchdogFile;

  public FileWatchdog(File file) {
    mWatchdogFile = file;
    mObserverInstance = new WatchdogFileObserver(mWatchdogFile.getAbsolutePath());
  }

  public abstract boolean onFileChanged();

  public File getFile() {
    return mWatchdogFile;
  }

  public void startWatch(boolean listenForLocalChanges) {
    if (DEBUG)
      Log.v(TAG, "starting...");

    mObserverInstance.setListerningForLocalChange(listenForLocalChanges);
    mObserverInstance.startWatching();
  }

  public void stopWatch() {
    if (DEBUG)
      Log.v(TAG, "stopping...");

    mObserverInstance.stopWatching();
  }

  public void lockFile() {
    mKnownMutationsInFlight.incrementAndGet();
  }

  public void unlockFile() {
    mKnownMutationsInFlight.decrementAndGet();
  }

  private class WatchdogFileObserver extends FileObserver {
    private final AtomicBoolean mIsDirty = new AtomicBoolean(false);
    private final String mPath;

    private boolean isListeningForLocalChanges = false;

    public WatchdogFileObserver(String path) {
      super(path, FileObserver.CLOSE_WRITE);
      mPath = path;
    }

    public void setListerningForLocalChange(boolean listen) {
      isListeningForLocalChanges = listen;
    }

    public void onEvent(int event, String path) {
      if (isListeningForLocalChanges) {
        int modsInFlight = mKnownMutationsInFlight.get();
        if (modsInFlight > 0) {
          Log.d(TAG, "our own modification");
          // our own modification.
          return;
        }

        if (DEBUG)
          Log.d(TAG, "external modification to " + mPath + "; event=" + event);

        if (!mIsDirty.compareAndSet(false, true)) {
          // already handled. (we get a few update events during an data write)
          return;
        }
      }

      if (DEBUG)
        Log.d(TAG, "updating our caches for " + mPath);

      onFileChanged();
      mIsDirty.set(false);
    }
  }
}
