package com.wlanadb;

import com.wlanadb.network.SingleInstanceChecker;
import com.wlanadb.service.WLanAdbController;
import com.wlanadb.ui.Main;
import com.wlanadb.utils.Log;
import org.eclipse.jface.util.Util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

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
     *
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

        final Main window = new Main();

        final SingleInstanceChecker.MainInstance checker;
        final int error = window.prepare();
        if (error == WLanAdbController.OK) {
            checker = new SingleInstanceChecker.MainInstance(new SingleInstanceChecker.OnSecondInstanceStartListener() {
                @Override
                public void onSecondInstanceStarted() {
                    window.show();
                }
            });
            checker.start();
        } else if (error == WLanAdbController.ERROR_UDP_ADDRESS_USED) {
            SingleInstanceChecker.ChildInstance.notifyMain();
            return;
        } else {
            window.handleError(error);
            return;
        }


        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.e(TAG, "Shutting down due to uncaught exception", e);

                checker.stop();
                window.close();
                System.exit(1);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                checker.stop();
                window.close();
            }
        });

        window.open();

        checker.stop();
    }

    /**
     * Return true iff we're running on a Mac
     */
    static boolean isMac() {
        // TODO: Replace usages of this method with
        // org.eclipse.jface.util.Util#isMac() when we switch to Eclipse 3.5
        // (ddms is currently built with SWT 3.4.2 from ANDROID_SWT)
        return Util.isMac();
//        return System.getProperty("os.name").startsWith("Mac OS"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
