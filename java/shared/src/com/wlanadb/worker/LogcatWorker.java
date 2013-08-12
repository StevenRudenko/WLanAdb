package com.wlanadb.worker;

import com.wlanadb.config.MyConfig;
import com.wlanadb.data.CommandProto.Command;
import com.wlanadb.data.LogcatLine;
import com.wlanadb.logcat.LogParser;
import com.wlanadb.utils.IOUtilities;
import com.wlanadb.utils.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;

public class LogcatWorker extends InputWorker {
    private static final String TAG = LogcatWorker.class.getSimpleName();
    private static final boolean DEBUG = MyConfig.DEBUG && false;

    public interface OnLogMessageListener {
        /**
         * Event to be invoked on new log message read.
         *
         * @param message
         */
        public void onLogMessage(LogcatLine message);
    }

    private BufferedReader in;
    private OnLogMessageListener listener;

    public LogcatWorker(OnLogMessageListener listener) {
        super();
        this.listener = listener;
    }

    @Override
    public void setInputStream(InputStream in) {
        super.setInputStream(in);
        this.in = new BufferedReader(new InputStreamReader(in));
    }

    @Override
    public boolean execute() {
        if (!super.execute())
            return false;

        try {
            String line;
            while (isRunning() && ((line = in.readLine()) != null)) {
                if (DEBUG)
                    Log.v(TAG, line);
                final LogcatLine log = LogParser.parse(line);
                if (listener != null)
                    listener.onLogMessage(log);
            }
            return true;
        } catch (SocketException ignore) {
            // it seems socket was closed before we terminated
            // well, we can't do anything except terminate
        } catch (IOException e) {
            Log.e(TAG, "Fail to read log input", e);
        } finally {
            IOUtilities.closeStream(in);
            terminate();
        }
        return false;
    }

    @Override
    public Command getCommand() {
        return Command.newBuilder().setCommand("logcat").build();
    }
}
