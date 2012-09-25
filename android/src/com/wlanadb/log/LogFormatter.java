package com.wlanadb.log;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

  @Override
  public String format(LogRecord r) {
    final StringBuilder builder = new StringBuilder();
    builder.append(r.getMillis()).append(" ").append(r.getMessage()).append("\n");
    return builder.toString();
  }

}
