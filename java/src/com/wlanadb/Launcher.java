package com.wlanadb;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import com.wlanadb.ui.Main;
import com.wlanadb.utils.Log;

public class Launcher {
  @SuppressWarnings("unused")
  private static final String TAG = Launcher.class.getSimpleName();

  private Launcher() {
  }

  /*
   * If a thread bails with an uncaught exception, bring the whole
   * thing down.
   */
  private static class UncaughtHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
      Log.e(TAG, "shutting down due to uncaught exception", e);
      System.exit(1);
    }
  }

  /**
   * Launch the application.
   * @param args
   */
  public static void main(String[] args) {
    // In order to have the AWT/SWT bridge work on Leopard, we do this little hack.
    if (isMac()) {
      RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
      System.setProperty(
          "JAVA_STARTED_ON_FIRST_THREAD_" + (rt.getName().split("@"))[0], //$NON-NLS-1$
          "1"); //$NON-NLS-1$
    }

    Thread.setDefaultUncaughtExceptionHandler(new UncaughtHandler());

    final Main window = new Main();
    window.open();
  }

  /** Return true iff we're running on a Mac */
  static boolean isMac() {
    // TODO: Replace usages of this method with
    // org.eclipse.jface.util.Util#isMac() when we switch to Eclipse 3.5
    // (ddms is currently built with SWT 3.4.2 from ANDROID_SWT)
    return System.getProperty("os.name").startsWith("Mac OS"); //$NON-NLS-1$ //$NON-NLS-2$
  }
}
