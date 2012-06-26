package com.wlancat.worker;

import java.io.InputStream;
import java.io.OutputStream;

import com.wlancat.data.CommandProto.Command;

public abstract class BaseWorker {
  public static interface WorkerListener {
    public void onError();
  }

  protected final WorkerListener listener;
  protected final Command command;

  public BaseWorker(Command command, InputStream in, OutputStream out, WorkerListener listener) {
    this.command = command;
    this.listener = listener;
  }

  public abstract void start();

  public abstract void stop();
}
