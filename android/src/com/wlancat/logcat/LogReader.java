package com.wlancat.logcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class LogReader {
  private static final String TAG = LogReader.class.getSimpleName();

  public interface OnLogMessageListener {
    /**
     * Event to be invoked on new log message read.
     * @param message
     */
    public void onLogMessage(String message);
  }

  private static final int MSG_LOG_MESSAGE = 1;

  private static final String LOGCAT_CMD = "logcat";

  @SuppressLint("HandlerLeak")
  private final Handler mUiTread = new Handler(Looper.getMainLooper()) {
    public void handleMessage(android.os.Message msg) {
      final String message = (String) msg.obj;
      listener.onLogMessage(message);
    };
  };

  /**
   * Thread used to read log messages.
   */
  private Thread mReadLogThread;

  /**
   * Indicates whether reader is running.
   */
  private volatile boolean isRunning = false;

  private OnLogMessageListener listener;

  public LogReader(OnLogMessageListener listener) {
    this.listener = listener;
  }

  /**
   * Starts logs reading process.
   */
  public void startOnNewTread() {
    mReadLogThread = new ReadLogsThread();
    mReadLogThread.start();
  }

  /**
   * Stops logs reading process.
   */
  public void stop() {
    isRunning = false;

    mReadLogThread = null;
  }

  public void start() {
    isRunning = true;

    BufferedReader reader = null;
    Process logcatProc = null;
    try {
      logcatProc = Runtime.getRuntime().exec(LOGCAT_CMD);

      reader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()));

      String line;
      while (isRunning && (line = reader.readLine()) != null) {
        if (!isRunning)
          break;

        if (line == null || line.length() == 0)
          continue;

        if (mReadLogThread != null) {
          final android.os.Message msg = mUiTread.obtainMessage(MSG_LOG_MESSAGE, line);
          mUiTread.sendMessage(msg);
        } else {
          listener.onLogMessage(line);
        }
      }
      Log.d(TAG, "LogCat reading finished!");
    } catch (IOException e) {
      Log.e(TAG, "Fail to read LogCat output", e);
    } finally {
      if (logcatProc != null) {
        logcatProc.destroy();
        logcatProc = null;
      }

      if (reader != null) {
        try {
          reader.close();
          reader = null;
        } catch (IOException e) {
          Log.e(TAG, "Fail to close LogCat reader stream", e);
        }
      }
    }
  }

  /**
   * Thread for reading log messages.
   * @author steven
   *
   */
  private class ReadLogsThread extends Thread {
    public ReadLogsThread() {
      setName(TAG);
      //setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void run() {
      LogReader.this.start();
    }
  };
}
