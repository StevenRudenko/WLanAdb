package com.wlancat.logcat;

import java.io.DataInputStream;
import java.io.IOException;

import android.util.Log;

import net.sf.signalslot_apt.annotations.signal;
import net.sf.signalslot_apt.annotations.signalslot;

@signalslot(force_concrete=true)
public abstract class LogReader {
  private static final String TAG = LogReader.class.getSimpleName();

  private final String LOGCAT_CMD = "logcat";

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

        onLogMessage(line);
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
      start();
    }
  };
}
