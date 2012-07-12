package com.wlancat.logcat;

import java.io.DataInputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import net.sf.signalslot_apt.annotations.signal;
import net.sf.signalslot_apt.annotations.signalslot;

@signalslot(force_concrete=true)
public abstract class LogReader {
  private static final String TAG = LogReader.class.getSimpleName();

  private static final int MSG_LOG_MESSAGE = 1;

  private static final String LOGCAT_CMD = "logcat";

  @SuppressLint("HandlerLeak")
  private final Handler mUiTread = new Handler(Looper.getMainLooper()) {
    public void handleMessage(android.os.Message msg) {
      final String message = (String) msg.obj;
      onLogMessage(message);
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

  /**
   * Signal to be invoked on new log message read.
   * @param message
   */
  @signal
  public abstract void onLogMessage(String message);

  /**
   * Starts logs reading process.
   */
  public void startOnTread() {
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

    DataInputStream stream = null;
    Process logcatProc = null;
    try {
      logcatProc = Runtime.getRuntime().exec(LOGCAT_CMD);
      stream = new DataInputStream(logcatProc.getInputStream());

      String line;
      while (isRunning && (line = stream.readLine()) != null) {
        if (!isRunning)
          break;

        if (line == null || line.length() == 0)
          continue;

        if (mReadLogThread != null) {
          final android.os.Message msg = mUiTread.obtainMessage(MSG_LOG_MESSAGE, line);
          mUiTread.sendMessage(msg);
        } else {
          onLogMessage(line);
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

      if (stream != null) {
        try {
          stream.close();
          stream = null;
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
    }

    @Override
    public void run() {
      LogReader.this.start();
    }
  };
}
