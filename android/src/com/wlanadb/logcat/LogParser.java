package com.wlanadb.logcat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wlanadb.data.LogcatLine;

public class LogParser {

  private static final Pattern LOG_LINE_PATTERN = Pattern.compile("^([A-Z])\\/(.*)\\(\\s*(\\d+)\\s*\\): (.*)$");

  public static LogcatLine parse(String logMessage) {
    final Matcher matcher = LOG_LINE_PATTERN.matcher(logMessage);
    if (!matcher.matches())
      return null;

    final LogcatLine logcatLine = new LogcatLine();
    logcatLine.type = matcher.group(1);
    logcatLine.tag = matcher.group(2);
    logcatLine.pid = Integer.parseInt(matcher.group(3));
    logcatLine.text = matcher.group(4);
    return logcatLine;
  }
}
