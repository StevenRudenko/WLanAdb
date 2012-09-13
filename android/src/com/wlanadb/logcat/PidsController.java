package com.wlanadb.logcat;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;

import com.wlanadb.utils.AndroidUtils;

public class PidsController {

  public interface OnPidsUpdateListener {
    public void onPidsUpdated(Collection<AndroidUtils.RunningProcess> processes);
  }

  private static final long PIDS_UPDATE_DELAY = 5000;
  private static final int MSG_PIDS_UPDATE = 1;

  private final ActivityManager mActivityManager;
  private final PackageManager mPackageManager;

  private final PidsUpdater mUpdater = new PidsUpdater();
  private final HashSet<OnPidsUpdateListener> mListeners = new HashSet<OnPidsUpdateListener>();

  private HashSet<AndroidUtils.RunningProcess> mProcesses = null;

  public PidsController(Context context) {
    mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    mPackageManager = context.getPackageManager();
  }

  public void start() {
    updateProcessesList();

    mUpdater.sendEmptyMessageDelayed(MSG_PIDS_UPDATE, PIDS_UPDATE_DELAY);
  }

  public void stop() {
    mUpdater.removeMessages(MSG_PIDS_UPDATE);
  }

  public Collection<AndroidUtils.RunningProcess> getRunningProcesses() {
    return mProcesses;
  }

  public void addOnPidsUpdateListener(OnPidsUpdateListener listener) {
    synchronized (mListeners) {
      mListeners.add(listener);
      if (mProcesses != null)
        listener.onPidsUpdated(mProcesses);
    }
  }

  public void removeOnPidsUpdateListener(OnPidsUpdateListener listener) {
    synchronized (mListeners) {
      mListeners.remove(listener);
    }
  }

  public void onPidsUpdated() {
    synchronized (mListeners) {
      for (OnPidsUpdateListener listener : mListeners) {
        if (listener != null)
          listener.onPidsUpdated(mProcesses);
      }
    }
  }

  private void updateProcessesList() {
    final List<AndroidUtils.RunningProcess> processes = AndroidUtils.getRunningProcesses(mActivityManager, mPackageManager);
    boolean needUpdate = false;

    final HashSet<AndroidUtils.RunningProcess> copy = new HashSet<AndroidUtils.RunningProcess>();
    if (mProcesses != null) {
      for (AndroidUtils.RunningProcess process : mProcesses) {
        if (processes.contains(process)) {
          copy.add(process);
        } else {
          needUpdate = true;
        }
      }
    }

    for (AndroidUtils.RunningProcess process : processes) {
      if (copy.add(process))
        needUpdate = true;
    }

    if (needUpdate) {
      mProcesses = copy;
      onPidsUpdated();
    }
  }

  private class PidsUpdater extends Handler {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      updateProcessesList();

      sendEmptyMessageDelayed(MSG_PIDS_UPDATE, PIDS_UPDATE_DELAY);
    }
  }
}
