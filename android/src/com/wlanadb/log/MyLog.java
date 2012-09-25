package com.wlanadb.log;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import android.content.Context;

public class MyLog {
  private static final String TAG = MyLog.class.getName();

  private static final String LOGGER_NAME = TAG;
  private static final int LOGGER_LIMIT = 2 * 1024 * 1024;

  private static Logger mLogger = null;

  public static void init(Context context) {
    mLogger = Logger.getLogger(LOGGER_NAME);
    mLogger.setLevel(Level.ALL);

    try {
      final File cacheDir = context.getExternalCacheDir();
      if (!cacheDir.exists()) {
        cacheDir.mkdirs();
      }
      final File logFile = new File(cacheDir, TAG);
      // create a file handler that uses the custom formatter
      final FileHandler handler = new FileHandler(logFile.getPath(), LOGGER_LIMIT, 1, true);
      handler.setFormatter(new LogFormatter());
      handler.setLevel(Level.ALL);
      mLogger.addHandler(handler);
    } catch (IOException e) {
    }

    // registering logger
    LogManager.getLogManager().addLogger(mLogger);
  }

  public static void v(String msg) {
    if (mLogger != null) {
      mLogger.info(msg);
    }
  }

  public static void i(String msg) {
    if (mLogger != null) {
      mLogger.fine(msg);
    }
  }

  public static void w(String msg) {
    if (mLogger != null) {
      mLogger.finest(msg);
    }
  }
}
