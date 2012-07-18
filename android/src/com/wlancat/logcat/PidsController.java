package com.wlancat.logcat;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;

import com.wlancat.utils.AndroidUtils;

public class PidsController {

  public interface OnPidsUpdateListener {
    public void onPidsUpdated(Collection<AndroidUtils.RunningProcess> pids);
  }

  private static final int MSG_PIDS_UPDATE = 1;
  private static final long DELAY_PIDS_UPDATE = 15000;

  private final ActivityManager mActivityManager;
  private final PackageManager mPackageManager;

  private final PidsUpdater mUpdater = new PidsUpdater();
  private final OnPidsUpdateListener mListener;

  public PidsController(Context context, OnPidsUpdateListener listener) {
    mListener = listener;

    mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    mPackageManager = context.getPackageManager();
  }

  public void start() {
    mUpdater.sendEmptyMessage(MSG_PIDS_UPDATE);
  }

  public void stop() {
    mUpdater.removeMessages(MSG_PIDS_UPDATE);
  }

  private class PidsUpdater extends Handler {
    private final HashSet<AndroidUtils.RunningProcess> mProcesses = new HashSet<AndroidUtils.RunningProcess>();

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);

      final List<AndroidUtils.RunningProcess> processes = AndroidUtils.getRunningProcesses(mActivityManager, mPackageManager);
      boolean needUpdate = false;

      for (AndroidUtils.RunningProcess process : mProcesses) {
        if (!processes.contains(process)) {
          mProcesses.remove(process);
          needUpdate = true;
        }
      }

      for (AndroidUtils.RunningProcess process : processes) {
        if (mProcesses.add(process))
          needUpdate = true;
      }

      if (needUpdate)
        mListener.onPidsUpdated(mProcesses);

      sendEmptyMessageDelayed(MSG_PIDS_UPDATE, DELAY_PIDS_UPDATE);
    }
  }
}
