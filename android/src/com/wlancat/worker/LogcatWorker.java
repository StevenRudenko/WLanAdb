package com.wlancat.worker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.wlancat.logcat.LogReader;
import com.wlancat.logcat.LogReaderSignalSlot;

import net.sf.signalslot_apt.SignalSlot;
import net.sf.signalslot_apt.annotations.signalslot;
import net.sf.signalslot_apt.annotations.slot;
import android.util.Log;

@signalslot
public class LogcatWorker extends BaseWorker {
  private static final String TAG = LogcatWorker.class.getSimpleName();
  private static final boolean DEBUG = false;

  private final OutputStreamWriter mOutputStream;
  private final LogReader mLogReader;

  public LogcatWorker(InputStream in, OutputStream out, WorkerListener listener) {
    super(in, out, listener);

    mLogReader = new LogReaderSignalSlot();
    mOutputStream = new OutputStreamWriter(out);
  }

  @slot
  public void onWriteLine(String line) {
    synchronized (this) {
      if (mOutputStream == null)
        return;

      try {
        mOutputStream.write(line);
        mOutputStream.write("\r\n");
        mOutputStream.flush();
      } catch (IOException e) {
        if (DEBUG)
          Log.e(TAG, "Fail to write line to stream: " + e.getMessage());
        listener.onError();
      }
    }
  }

  @Override
  public void start() {
    SignalSlot.connect(mLogReader, LogReaderSignalSlot.Signals.ONLOGMESSAGE_STRING, this, LogcatWorkerSignalSlot.Slots.ONWRITELINE_STRING);
    mLogReader.start();
  }

  @Override
  public void stop() {
    mLogReader.stop();
  }
}
