package com.wlanadb.worker;

import com.wlanadb.data.CommandProto.Command;

public interface CommandProcessor {
  public BaseWorker getWorker(Command command);
}
