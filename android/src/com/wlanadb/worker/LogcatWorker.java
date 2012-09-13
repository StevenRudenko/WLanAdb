package com.wlanadb.worker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.util.Log;

import com.wlanadb.config.MyConfig;
import com.wlanadb.data.CommandProto.Command;
import com.wlanadb.logcat.LogFilter;
import com.wlanadb.logcat.LogReader;
import com.wlanadb.logcat.PidsController;
import com.wlanadb.logcat.LogReader.OnLogMessageListener;

public class LogcatWorker extends BaseWorker implements OnLogMessageListener {
  private static final String TAG = LogcatWorker.class.getSimpleName();
  private static final boolean DEBUG = MyConfig.DEBUG && true;

  private static final String PARAM_APP = "--app=";
  private static final String PARAM_PID = "--pid=";
  private static final String PARAM_TYPE = "--type=";

  private final LogFilter mLogFilter;
  private final LogReader mLogReader;

  private BufferedWriter mOutputStream;
  private PidsController mPidsController;

  public LogcatWorker(Command command) {
    super(command);

    mLogReader = new LogReader(this);

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

  @Override
  public void setOutputStream(OutputStream out) {
    super.setOutputStream(out);
    mOutputStream = new BufferedWriter(new OutputStreamWriter(out));
  }

  public void setPidsController(PidsController pidsController) {
    mPidsController = pidsController;
  }

  @Override
  public synchronized void onLogMessage(String message) {
    if (mLogFilter != null && mLogFilter.filter(message))
      return;

    try {
      mOutputStream.write(message);
      mOutputStream.newLine();
      mOutputStream.flush();
    } catch (IOException e) {
      if (DEBUG)
        Log.e(TAG, "Fail to write line to stream: " + e.getMessage());
      terminate();
      if (listener != null)
        listener.onError();
    }
  }

  @Override
  public boolean execute() {
    if (mLogFilter != null) {
      mPidsController.addOnPidsUpdateListener(mLogFilter);
      mPidsController.start();

      if (DEBUG)
        Log.v(TAG, "Filter: " + mLogFilter);
    }

    mLogReader.start();

    return true;
  }

  @Override
  public void terminate() {
    mLogReader.stop();

    if (mLogFilter != null) {
      mPidsController.removeOnPidsUpdateListener(mLogFilter);
      mPidsController.stop();
    }
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