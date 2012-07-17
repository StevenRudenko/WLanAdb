package com.wlancat.worker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.util.Log;

import com.wlancat.data.CommandProto.Command;
import com.wlancat.logcat.LogReader;
import com.wlancat.logcat.LogReader.OnLogMessageListener;

public class LogcatWorker extends BaseWorker implements OnLogMessageListener {
  private static final String TAG = LogcatWorker.class.getSimpleName();
  private static final boolean DEBUG = false;

  private final BufferedWriter mOutputStream;
  private final LogReader mLogReader;

  public LogcatWorker(Command command, InputStream in, OutputStream out, WorkerListener listener) {
    super(command, in, out, listener);

    mLogReader = new LogReader(this);
    mOutputStream = new BufferedWriter(new OutputStreamWriter(out));
  }

  @Override
  public void onLogMessage(String message) {
    synchronized (this) {
      if (mOutputStream == null)
        return;

      try {
        mOutputStream.write(message);
        mOutputStream.newLine();
        //mOutputStream.write("\r\n");
        //mOutputStream.flush();
      } catch (IOException e) {
        if (DEBUG)
          Log.e(TAG, "Fail to write line to stream: " + e.getMessage());
        listener.onError();
      }
    }
  }

  @Override
  public void start() {
    mLogReader.start();
  }

  @Override
  public void stop() {
    mLogReader.stop();
  }
}
