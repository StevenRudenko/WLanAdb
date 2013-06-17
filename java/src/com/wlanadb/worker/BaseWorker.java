package com.wlanadb.worker;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.wlanadb.data.CommandProto.Command;
import com.wlanadb.utils.Log;

public abstract class BaseWorker {
  private static final String TAG = BaseWorker.class.getSimpleName();

  public static interface WorkerListener {
    public void onWorkerFinished();
    public void onError();
  }

  private WorkerListener listener;

  private DataOutputStream out;
  private volatile boolean isRunning = false;

  public BaseWorker() {
  }

  public void setInputStream(InputStream in) {
  }

  public void setOutputStream(OutputStream out) {
    this.out = new DataOutputStream(out);
  }

  public void setWorkerListener(WorkerListener listener) {
    this.listener = listener;
  }
  
  public WorkerListener getWorkerListener() {
    return listener;
  }

  public abstract Command getCommand();

  public boolean isRunning() {
    return isRunning;
  }

  public boolean execute() {
    try {
      isRunning = true;

      final byte[] cmd = getCommand().toByteArray();
      out.writeInt(cmd.length);
      out.write(cmd);
      out.flush();

      return true;
    } catch (IOException e) {
      Log.e(TAG, "Fail to send command to client", e);
    }

    isRunning = false;
    return false;
  }

  public void terminate() {
    isRunning = false;
  }
}
