package com.wlanadb.network;

import com.wlanadb.config.MyConfig;
import com.wlanadb.data.ClientProto.Client;
import com.wlanadb.utils.Log;
import com.wlanadb.worker.BaseWorker;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class P2PClient implements Runnable {
    private static final String TAG = P2PClient.class.getSimpleName();
    private static final boolean DEBUG = MyConfig.DEBUG && true;

    private final Client client;

    private Socket socket;
    private Thread workerThread;

    private BaseWorker worker;

    private volatile boolean isRunning = false;

    public P2PClient(Client client) {
        this.client = client;
    }

    public void start(BaseWorker worker) {
        if (isRunning)
            stop();

        isRunning = true;

        try {
            socket = new Socket(client.getIp(), client.getPort());

            this.worker = worker;
            worker.setInputStream(socket.getInputStream());
            worker.setOutputStream(socket.getOutputStream());

        } catch (UnknownHostException e) {
            if (DEBUG)
                Log.e(TAG, "Can't connect to client: " + client.getIp() + ":" + client.getPort(), e);
        } catch (IOException e) {
            if (DEBUG)
                Log.e(TAG, "Can't open streams to read/write data", e);
        }

        workerThread = new Thread(this);
        workerThread.start();
    }

    public void stop() {
        isRunning = false;

        if (worker != null) {
            worker.terminate();
        }

        if (workerThread != null) {
            final Thread inherited = workerThread;
            workerThread = null;
            inherited.interrupt();
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            if (DEBUG)
                Log.e(TAG, "Can't close server socket", e);
        }
    }

    @Override
    public void run() {
        worker.execute();
    }

}
