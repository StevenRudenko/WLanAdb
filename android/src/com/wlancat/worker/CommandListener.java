package com.wlancat.worker;

public class CommandListener {

  private final CommandProcessor mCommandProcessor;

  public CommandListener(CommandProcessor commandProcessor) {
    this.mCommandProcessor = commandProcessor;
  }

  public CommandProcessor getCommandProcessor() {
    return mCommandProcessor;
  }
}
