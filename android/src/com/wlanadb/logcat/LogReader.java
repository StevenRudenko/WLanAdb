package com.wlanadb.logcat;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Scanner;

import com.wlanadb.config.MyConfig;
import com.wlanadb.utils.IOUtilities;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class LogReader {
    private static final String TAG = LogReader.class.getSimpleName();
    private static final boolean DEBUG = MyConfig.DEBUG && true;

    public static final int LOGCAT_BUFFER_SIZE = 16 * 1024;

    public interface OnLogMessageListener {
        /**
         * Event to be invoked on new log message read.
         * @param message
         */
        public void onLogMessage(String message);
    }

    private static final int MSG_LOG_MESSAGE = 1;

    private static final String LOGCAT_CMD = "logcat";

    private final String[] params;

    @SuppressLint("HandlerLeak")
    private Handler mReadLogsHandler = null;

    /**
     * Thread used to read log messages.
     */
    private Thread mReadLogThread;

    /**
     * Indicates whether reader is running.
     */
    private volatile boolean isRunning = false;

    private final OnLogMessageListener listener;

    private DataInputStream reader = null;

    public LogReader(OnLogMessageListener listener) {
        this(listener, null);
    }

    public LogReader(OnLogMessageListener listener, String[] params) {
        this.listener = listener;
        this.params = params;
    }

    /**
     * Starts logs reading process.
     */
    public void startOnNewTread() {
        stop();

        mReadLogsHandler = new ReadLogsHandler();

        mReadLogThread = new ReadLogsThread();
        mReadLogThread.start();
    }

    /**
     * Stops logs reading process.
     */
    public void stop() {
        isRunning = false;

        mReadLogsHandler = null;
        if (mReadLogThread != null) {
            final Thread inherited = mReadLogThread;
            mReadLogThread = null;
            inherited.interrupt();
        }

        IOUtilities.closeStream(reader);
    }

    public void start() {
        isRunning = true;

        Process logcatProc = null;
        try {
            final StringBuilder paramsString = new StringBuilder();
            if (params != null) {
                for (String param : params) {
                    paramsString.append(" ");
                    paramsString.append(param);
                }
            }
            logcatProc = Runtime.getRuntime().exec(LOGCAT_CMD + paramsString.toString());

            reader = new DataInputStream(logcatProc.getInputStream());

            final byte[] b = new byte[LOGCAT_BUFFER_SIZE];
            int read;
            String previousPart = "";
            while (isRunning && ((read = reader.read(b)) > 0)) {
                final String data = previousPart + new String(b, 0, read);
                previousPart = "";

                final boolean fullyRead = data.endsWith("\n");
                final String[] lines = data.split("\n");
                final int count = lines.length;
                for (int i=0; i<count; ++i) {
                    final String line = lines[i];
                    if (i == count - 1 && !fullyRead) {
                        previousPart = line;
                        break;
                    }

                    if (mReadLogsHandler != null) {
                        final android.os.Message msg = mReadLogsHandler.obtainMessage(MSG_LOG_MESSAGE, line);
                        mReadLogsHandler.sendMessage(msg);
                    } else {
                        listener.onLogMessage(line);
                    }
                }
            }

            if (DEBUG)
                Log.d(TAG, "LogCat reading finished!");
        } catch (IOException e) {
            if (DEBUG)
                Log.e(TAG, "Fail to read LogCat output", e);
        } finally {
            if (logcatProc != null) {
                logcatProc.destroy();
                logcatProc = null;
            }

            IOUtilities.closeStream(reader);
        }
    }

    /**
     * Thread for reading log messages.
     * @author steven
     *
     */
    private class ReadLogsThread extends Thread {
        public ReadLogsThread() {
            setName(TAG);
        }

        @Override
        public void run() {
            LogReader.this.start();
        }
    };

    private class ReadLogsHandler extends Handler {
        public ReadLogsHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            final String message = (String) msg.obj;
            listener.onLogMessage(message);
        };
    }
}
