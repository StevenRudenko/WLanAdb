package com.wlancat.worker;

import java.io.InputStream;
import java.io.OutputStream;

import com.wlancat.data.CommandProto.Command;

public abstract class BaseWorker {
  public static interface WorkerListener {
    public void onSuccess();
    public void onError();
  }

  protected final Command command;

  protected WorkerListener listener;

  public BaseWorker(Command command) {
    this.command = command;
  }

  public void setInputStream(InputStream in) {
  }

  public void setOutputStream(OutputStream out) {
  }
  
  public void setWorkerListener(WorkerListener listener) {
    this.listener = listener;
  }

  public abstract boolean execute();

  public abstract void terminate();
}
