package com.wlancat.worker;

import com.wlancat.data.CommandProto.Command;

public interface CommandProcessor {
  public BaseWorker getWorker(Command command);
}
