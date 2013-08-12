package com.wlanadb.service;

import com.wlanadb.data.LogcatLine;
import com.wlanadb.worker.LogcatWorker.OnLogMessageListener;

import java.util.LinkedList;
import java.util.List;

public class LogManager implements OnLogMessageListener {
    @SuppressWarnings("unused")
    private static final String TAG = LogManager.class.getSimpleName();

    private final List<LogcatLine> log = new LinkedList<LogcatLine>();

    private final OnLogMessageListener listener;

    public LogManager(OnLogMessageListener listener) {
        this.listener = listener;
    }

    public void reset() {
        synchronized (log) {
            log.clear();
        }

        if (listener != null)
            listener.onLogMessage(null);
    }

    public LogcatLine[] getLogs() {
        synchronized (log) {
            final LogcatLine[] result = new LogcatLine[log.size()];
            log.toArray(result);
            return result;
        }
    }

    @Override
    public void onLogMessage(LogcatLine message) {
        synchronized (log) {
            log.add(message);
        }

        if (listener != null)
            listener.onLogMessage(message);
    }

}
