package com.wlanadb.logcat;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.wlanadb.config.MyConfig;
import com.wlanadb.utils.IOUtilities;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class LogReader {
  private static final String TAG = LogReader.class.getSimpleName();
  private static final boolean DEBUG = MyConfig.DEBUG && true;

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
  private Handler mReadLogsHandler = null;

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
    mReadLogsHandler = new ReadLogsHandler();

    mReadLogThread = new ReadLogsThread();
    mReadLogThread.start();
  }

  /**
   * Stops logs reading process.
   */
  public void stop() {
    isRunning = false;

    mReadLogsHandler = null;
    mReadLogThread = null;
  }

  public void start() {
    isRunning = true;

    BufferedReader reader = null;
    Process logcatProc = null;
    try {
      logcatProc = Runtime.getRuntime().exec(LOGCAT_CMD);

      reader = new BufferedReader(new InputStreamReader(new DataInputStream(logcatProc.getInputStream())));

      String line;
      while (isRunning && (line = reader.readLine()) != null) {
        if (mReadLogThread != null) {
          final android.os.Message msg = mReadLogsHandler.obtainMessage(MSG_LOG_MESSAGE, line);
          mReadLogsHandler.sendMessage(msg);
        } else {
          listener.onLogMessage(line);
        }
      }
      if (DEBUG)
        Log.d(TAG, "LogCat reading finished!");
    } catch (IOException e) {
      if (DEBUG)
        Log.e(TAG, "Fail to read LogCat output", e);
    } finally {
      if (logcatProc != null) {
        logcatProc.destroy();
        logcatProc = null;
      }

      IOUtilities.closeStream(reader);
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

  private class ReadLogsHandler extends Handler {
    public ReadLogsHandler() {
      super(Looper.getMainLooper());
    }

    @Override
    public void handleMessage(android.os.Message msg) {
      final String message = (String) msg.obj;
      listener.onLogMessage(message);
    };
  }
}
