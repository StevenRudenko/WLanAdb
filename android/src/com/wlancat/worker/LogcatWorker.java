package com.wlancat.worker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.util.Log;

import com.wlancat.data.LogcatLine;
import com.wlancat.data.CommandProto.Command;
import com.wlancat.logcat.LogFilter;
import com.wlancat.logcat.LogParser;
import com.wlancat.logcat.LogReader;
import com.wlancat.logcat.PidsController;
import com.wlancat.logcat.LogReader.OnLogMessageListener;

public class LogcatWorker extends BaseWorker implements OnLogMessageListener {
  private static final String TAG = LogcatWorker.class.getSimpleName();
  private static final boolean DEBUG = true;

  private static final String PARAM_APP = "--app=";
  private static final String PARAM_PID = "--pid=";
  private static final String PARAM_TYPE = "--type=";

  private final BufferedWriter mOutputStream;
  private final LogReader mLogReader;

  private PidsController mPidsController;
  private final LogFilter mLogFilter;

  public LogcatWorker(Command command, InputStream in, OutputStream out, WorkerListener listener) {
    super(command, in, out, listener);

    mLogReader = new LogReader(this);
    mOutputStream = new BufferedWriter(new OutputStreamWriter(out));

    final int count = command.getParamsCount();
    if (count == 0) {
      mLogFilter = null;
      if (DEBUG)
        Log.v(TAG, "No parameters were set");
    } else {
      if (DEBUG)
        Log.v(TAG, "There were " + count + " parameters set");
      mLogFilter = new LogFilter();
    }

    for (int i=0; i<count; ++i) {
      final String param = command.getParams(i);
      if (DEBUG)
        Log.v(TAG, "" + i + " parameter: " + param);

      parseFilterParameter(param);
    }
  }

  public void setPidsController(PidsController pidsController) {
    mPidsController = pidsController;
  }

  @Override
  public synchronized void onLogMessage(String message) {
    if (mOutputStream == null)
      return;

    if (mLogFilter != null && mLogFilter.filter(message))
      return;

    try {
      mOutputStream.write(message);
      mOutputStream.newLine();
      mOutputStream.flush();
    } catch (IOException e) {
      if (DEBUG)
        Log.e(TAG, "Fail to write line to stream: " + e.getMessage());
      listener.onError();
    }
  }

  @Override
  public void start() {
    mPidsController.addOnPidsUpdateListener(mLogFilter);
    mPidsController.start();

    if (DEBUG && mLogFilter != null)
      Log.v(TAG, "Filter: " + mLogFilter);

    mLogReader.start();
  }

  @Override
  public void stop() {
    mPidsController.removeOnPidsUpdateListener(mLogFilter);
    mPidsController.stop();
    mLogReader.stop();
  }

  private void parseFilterParameter(String param) {
    if (param.startsWith(PARAM_APP)) {
      mLogFilter.setApp(param.replaceFirst(PARAM_APP, ""), true);
    } else if (param.startsWith(PARAM_PID)) {
      final int pid;
      try {
        pid = Integer.parseInt(param.replaceFirst(PARAM_PID, ""));
      } catch (NumberFormatException e) {
        return;
      }

      mLogFilter.setPid(pid, true);
    } else if (param.startsWith(PARAM_TYPE)) {
      final String types = param.replaceFirst(PARAM_TYPE, "");
      mLogFilter.setType(LogFilter.TYPE_ALL, false);

      for (String type : types.split("")) {
        mLogFilter.setType(type, true);
      }
    } else {
      mLogFilter.setSearchTerm(param);
    }
  }
}
