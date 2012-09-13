package com.wlanadb.logcat;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.util.Log;

import com.wlanadb.config.MyConfig;
import com.wlanadb.data.LogcatLine;
import com.wlanadb.logcat.PidsController.OnPidsUpdateListener;
import com.wlanadb.utils.AndroidUtils.RunningProcess;

public class LogFilter implements OnPidsUpdateListener {
  private static final String TAG = LogFilter.class.getSimpleName();
  private static final boolean DEBUG = MyConfig.DEBUG && true;

  public static final String TYPE_V = "V";
  public static final String TYPE_D = "D";
  public static final String TYPE_I = "I";
  public static final String TYPE_W = "W";
  public static final String TYPE_E = "E";
  public static final String TYPE_A = "A";

  public static final String TYPE_ALL = TYPE_V + TYPE_D + TYPE_I + TYPE_W + TYPE_E + TYPE_A;

  private static final Pattern START_PROC_PATTERN = Pattern.compile("^[A-Z]\\/ActivityManager\\(\\s*\\d+\\s*\\): Start proc (.*?) .*?: pid=(\\d+).*");
  private static final Pattern END_PROC_PATTERN = Pattern.compile("^[A-Z]\\/ActivityManager\\(\\s*\\d+\\s*\\): Process (.*?) \\(pid (\\d+)\\) has died.");

  private String types = TYPE_ALL;
  private final HashSet<Integer> pids = new HashSet<Integer>();
  private final HashSet<String> apps = new HashSet<String>();
  private String searchTerm;

  public LogFilter() {
  }

  @Override
  public void onPidsUpdated(Collection<RunningProcess> processes) {
    if (apps.isEmpty())
      return;

    for (RunningProcess process : processes) {
      if (apps.contains(process.processName)) {
        setPid(process.pid, true);
      }
    }
  }

  public LogFilter setType(String type, boolean show) {
    if (type == null)
      return this;

    if (TYPE_ALL.equals(type)) {
      if (show)
        types = TYPE_ALL;
      else
        types = "";
      return this;
    }

    if (!show)
      types = types.replaceAll(type, "");
    else if (!types.contains(type))
      types = types + type;
    return this;
  }

  public LogFilter setPid(int pid, boolean show) {
    if (!show)
      pids.remove(pid);
    else
      pids.add(pid);
    return this;
  }

  public LogFilter setApp(String app, boolean show) {
    if (!show)
      apps.remove(app);
    else
      apps.add(app);

    return this;
  }

  public LogFilter setSearchTerm(String searchTerm) {
    this.searchTerm = searchTerm;
    return this;
  }

  public boolean filterAll() {
    return TextUtils.isEmpty(types);
  }

  public boolean filter(LogcatLine logLine) {
    if (TextUtils.isEmpty(types) || !types.contains(logLine.type))
      return true;

    if (!pids.isEmpty() && !pids.contains(logLine.pid))
      return true;

    if (!TextUtils.isEmpty(searchTerm) && !logLine.text.contains(searchTerm))
      return true;

    return false;
  }

  public boolean filter(String logMessage) {
    final LogcatLine logLine = LogParser.parse(logMessage);
    if (logLine == null) {
      if (DEBUG)
        Log.w(TAG, "Fail to parse line: " + logMessage);
      return true;
    }

    if (apps.isEmpty())
      return filter(logLine);

    final Matcher startProc = START_PROC_PATTERN.matcher(logMessage);
    if (startProc.matches()) {
      final String processName = startProc.group(1);

      if (apps.contains(processName)) {
        final int pid = Integer.parseInt(startProc.group(2));
        pids.add(pid);
        return false;
      }
    } else {
      final Matcher endProc = END_PROC_PATTERN.matcher(logMessage);
      if (endProc.matches()) {
        final String processName = endProc.group(1);

        if (apps.contains(processName)) {
          final int pid = Integer.parseInt(endProc.group(2));
          pids.remove(pid);
          return false;
        }
      }
    }

    // filter put lines if we didn't find pids for apps
    if (pids.isEmpty())
      return true;

    return filter(logLine);
  }

  @Override
  public String toString() {
    final StringBuilder result = new StringBuilder();
    result.append("TYPES: [")
    .append(types)
    .append("] PIDS: [");
    for (Integer pid : pids) {
      result.append(pid + " ");
    }

    result.append("] APPS: [");
    for (String app : apps) {
      result.append(app + " ");
    }

    result.append("] SEARCH: [");
    if (searchTerm != null)
      result.append(searchTerm);
    result.append("]");

    return result.toString();
  }
}
