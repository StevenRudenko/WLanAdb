package com.wlanadb.network;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wlanadb.config.MyConfig;
import com.wlanadb.data.CommandProto.Command;
import com.wlanadb.utils.IOUtilities;
import com.wlanadb.worker.BaseWorker;
import com.wlanadb.worker.CommandListener;
import com.wlanadb.worker.CommandProcessor;

public class P2PConnection extends CommandListener implements Runnable {
    private static final String TAG = P2PConnection.class.getSimpleName();
    private static final boolean DEBUG = MyConfig.DEBUG && true;

    private final Socket mSocket;

    private Command command;
    private BaseWorker worker;

    protected P2PConnection(Socket socket, CommandProcessor commandProcessor) {
        super(commandProcessor);

        mSocket = socket;
    }

    private void close() {
        if (DEBUG)
            Log.d(TAG, "Closing connection...");

        if (worker != null) {
            worker.terminate();
            worker = null;
        }

        if (mSocket.isClosed())
            return;

        try {
            IOUtilities.closeStream(mSocket.getOutputStream());
            mSocket.shutdownOutput();
        } catch (IOException ignore) {
            // do nothing
        }

        try{
            IOUtilities.closeStream(mSocket.getInputStream());
            mSocket.shutdownInput();
        } catch (IOException ignore) {
            // do nothing
        }

        try {
            mSocket.close();
        } catch (Exception ignore) {
            Log.e(TAG, "Fail to close Socket", ignore);
        }

        if (DEBUG)
            Log.d(TAG, "Connection was closed");
    }

    public void run() {
        try {
            final DataInputStream mInputStream = new DataInputStream(mSocket.getInputStream());
            final int commandLength = mInputStream.readInt();

            if (DEBUG)
                Log.d(TAG, "Command size: " + commandLength);

            byte[] buffer = new byte[commandLength];
            mInputStream.read(buffer);

            try {
                Log.d(TAG, "Parsing Command...");
                command = Command.parseFrom(buffer);
                buffer = null;
            } catch (InvalidProtocolBufferException e) {
                Log.w(TAG, "Fail to parse command", e);
            }

            final CommandProcessor commandProcessor = getCommandProcessor();
            if (commandProcessor == null) {
                close();
                return;
            }

            worker = commandProcessor.getWorker(command);
            if (worker == null) {
                close();
                return;
            }

            worker.setInputStream(mSocket.getInputStream());
            worker.setOutputStream(mSocket.getOutputStream());
            worker.execute();
        } catch (IOException e) {
            Log.e(TAG, "Error while communicating with client", e);
        } finally {
            close();
        }
    }
}