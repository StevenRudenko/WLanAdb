package com.wlanadb.log;

public interface AnalyticsEvents {

  public static final String CAT_SERVICE = "Service";
  public static final String CAT_COMMAND = "Command";
  public static final String CAT_SETTINGS = "Settings";

  public static final String ACTION_START_SERVICE = "Start Service";
  public static final String ACTION_STOP_SERVICE = "Stop Service";
  public static final String ACTION_SETTINGS_CHANGED = "Settings changed";
  public static final String ACTION_COMMAND = "Command";

  public static final String LABEL_OK = "ok";

  public static final String LABEL_NO_WIFI = "no-wifi";
  public static final String LABEL_WIFI_DISABLED = "wifi-disabled";
  public static final String LABEL_NOT_TRUSTED_HOTSPOT = "not-trusted-hotspot";
  public static final String LABEL_NO_LOCAL_ADDRESS = "no-local-address";

  public static final String LABEL_PIN = "pin";
  public static final String LABEL_NAME = "name";
  public static final String LABEL_TRUSTED_HOTSPOTS = "trusted-hotspots";

  public static final String LABEL_LOGCAT = "logcat";
  public static final String LABEL_PUSH = "push";
  public static final String LABEL_INSTALL = "install";
  public static final String LABEL_UNKNOWN = "unknown";
}
