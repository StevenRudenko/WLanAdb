package com.wlanadb.worker;

import com.wlanadb.data.CommandProto;
import com.wlanadb.utils.IOUtilities;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: steven
 * Date: 7/29/13
 * Time: 2:54 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class OutputWorker extends BaseWorker {
    private InputStream in;

    private Thread mTerminateHandlerThread;

    public OutputWorker(CommandProto.Command command) {
        super(command);
    }

    @Override
    public void setInputStream(InputStream in) {
        super.setInputStream(in);
        this.in = in;
    }

    @Override
    public boolean execute() {
        mTerminateHandlerThread = new Thread(new TerminateHandler());
        mTerminateHandlerThread.start();

        return true;
    }

    @Override
    public void terminate() {

        if (mTerminateHandlerThread != null) {
            final Thread inherited = mTerminateHandlerThread;
            mTerminateHandlerThread = null;
            inherited.interrupt();
        }
        IOUtilities.closeStream(in);
    }

    private class TerminateHandler implements Runnable {

        @Override
        public void run() {
            try {
                // block thread until first data income
                in.read();
                terminate();
            } catch (IOException ignore) {
                // we don't care if we failed
            }
        }
    }
}
