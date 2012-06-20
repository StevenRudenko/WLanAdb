package com.wlancat.worker;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class BaseWorker {

  public static interface WorkerListener {
    public void onError();
  }

  protected final WorkerListener listener;

  public BaseWorker(InputStream in, OutputStream out, WorkerListener listener) {
    this.listener = listener;
  }

  public abstract void start();

  public abstract void stop();
}
