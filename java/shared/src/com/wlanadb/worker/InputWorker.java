package com.wlanadb.worker;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: steven
 * Date: 7/29/13
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class InputWorker extends BaseWorker {

    private OutputStream out;

    @Override
    public void setOutputStream(OutputStream out) {
        super.setOutputStream(out);
        this.out = out;
    }

    @Override
    public void terminate() {
        try {
            out.write(0);
        } catch (IOException ignore) {
            // we tried but we failed
        }

        super.terminate();
    }
}
