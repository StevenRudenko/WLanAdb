package com.wlanadb.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.wlanadb.config.MyConfig;
import com.wlanadb.service.ConnectionsStatusReciever;

public class BroadcastServer implements Runnable {
    private static final String TAG = BroadcastServer.class.getSimpleName();
    private static final boolean DEBUG = MyConfig.DEBUG && true;

    public interface BroadcastMessageHandler {
        public ByteString onDataPackageRecieved(ByteString data);
    }

    public interface TestConnectionListener {
        public void onTestCompleted(int status);
    }

    public static final int BROADCAST_PORT = 44533;
    public static final int MESSAGE_LISTEN_TIMEOUT = 500;
    public static final int MESSAGE_BUFFER = 512;

    private InetAddress mBroadcastAddress;
    private InetAddress mLocalAddress;

    private Thread mRecieveThread;
    private DatagramSocket mSocket;

    private BroadcastMessageHandler mHandler;
    private TestConnectionHandler mTestConnectionHandler;

    private MulticastLock mMulticastLock;
    private volatile boolean isRunning = false;

    public BroadcastServer(BroadcastMessageHandler handler) {
        mHandler = handler;
    }

    public void start(WifiManager wifiManager, InetAddress broadcastAddress, InetAddress localAddress) {
        mTestConnectionHandler = new TestConnectionHandler();

        mMulticastLock = wifiManager.createMulticastLock(TAG);
        mMulticastLock.acquire();

        mBroadcastAddress = broadcastAddress;
        mLocalAddress = localAddress;
        if (DEBUG) {
            Log.d(TAG, "Updating addresses:");
            Log.d(TAG, "- broadcast address: " + mBroadcastAddress.getHostAddress());
            Log.d(TAG, "- local address: " + mLocalAddress.getHostAddress());
            Log.d(TAG, "- port: " + BROADCAST_PORT);
        }

        isRunning = true;

        try {
            mSocket = new DatagramSocket(BROADCAST_PORT);
            mSocket.setBroadcast(true);
            mSocket.setSoTimeout(MESSAGE_LISTEN_TIMEOUT);
            mSocket.setReceiveBufferSize(4 * MESSAGE_BUFFER);
        } catch (IOException e) {
            Log.e(TAG, "Could not open socket", e);
            return;
        }

        isRunning = true;

        synchronized (this) {
            mRecieveThread = new Thread(this);
            mRecieveThread.start();
        }
    }

    public void stop() {
        if (DEBUG)
            Log.d(TAG, "Closing socket...");

        isRunning = false;

        mSocket.close();

        if (mMulticastLock != null && mMulticastLock.isHeld()) {
            mMulticastLock.release();
            mMulticastLock = null;
        }

        mTestConnectionHandler = null;
    }

    public void testConnection(TestConnectionListener listener)  {
        if (mTestConnectionHandler == null) {
            listener.onTestCompleted(ConnectionsStatusReciever.STATUS_UNDERFINED);
            return;
        }

        mTestConnectionHandler.setListener(listener);
        mTestConnectionHandler.start();
    }

    public void send(final ByteString data, final InetAddress reciever) {
        new Thread(TAG+":SEND") {
            @Override
            public void run() {
                super.run();
                try {
                    final byte[] message = data.toByteArray();
                    final InetAddress host = reciever == null ? mBroadcastAddress : reciever;
                    final DatagramPacket packet = new DatagramPacket(message, message.length,
                            host, BROADCAST_PORT);
                    if (isRunning)
                        mSocket.send(packet);
                } catch (IOException e) {
                    Log.e(TAG, "Failed broadcast data", e);
                }
            }
        }.start();
    }

    /**
     * Send a UDP packet to specific address. If address is broadcast address then message will be send broadcast.
     */
    public void send(final byte[] data, final InetAddress address) {
        new Thread(TAG+":SEND") {
            @Override
            public void run() {
                super.run();
                try {
                    final InetAddress reciever = address == null ? mBroadcastAddress : address;
                    if (DEBUG)
                        Log.d(TAG, "Sending data to " + reciever.getHostAddress());
                    final DatagramPacket packet = new DatagramPacket(data, data.length,
                            reciever, BROADCAST_PORT);
                    if (isRunning)
                        mSocket.send(packet);
                } catch (IOException e) {
                    Log.e(TAG, "Data send failed to " +
                            (address == null ?  "ALL" : address.getHostAddress()), e);
                }
            }
        }.start();
    }

    public void run() {
        if (DEBUG)
            Log.d(TAG, "Starting reciever loop... ");
        isRunning = true;

        final String localHostAddress = mLocalAddress.getHostAddress();
        final byte[] buf = new byte[MESSAGE_BUFFER];
        // Loop and try to receive messages. We'll get back the packet we just
        // sent out, which isn't terribly helpful, but we'll discard it.
        do {
            if (mSocket.isClosed()) {
                if (DEBUG)
                    Log.d(TAG, "Stoping reciever loop...");
                return;
            }

            final DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                packet.setData(buf);
                mSocket.receive(packet);
            } catch (SocketTimeoutException ignore) {
                // it is normal behavior
                continue;
            } catch (IOException ignore) {
                // this error can be caused by closing socket only
                // ignoring it
                continue;
            }

            final InetAddress senderAddress = packet.getAddress();
            final String senderHostAddress = senderAddress.getHostAddress();

            final int packetLength = packet.getLength();
            final byte[] packetData = packet.getData();

            final ByteString data = ByteString.copyFrom(packetData, 0, packetLength);

            if (DEBUG)
                Log.d(TAG, "Recieved message from " + senderHostAddress + " : " + data.toStringUtf8());

            // it is test package we need ignore it from other clients
            if (mTestConnectionHandler.isTestPhrase(data)) {
                mTestConnectionHandler.passed();
                continue;
            }

            // ignoring packages from ourself
            if (localHostAddress.equals(senderHostAddress)) {
                continue;
            }

            final ByteString response = mHandler.onDataPackageRecieved(data);
            if (response != null)
                send(response, senderAddress);
        } while (isRunning);

        stop();
    }

    private class TestConnectionHandler extends Handler {
        private final String TEST_PHRASE = TAG+":TEST";
        private final ByteString TEST_PACKET = ByteString.copyFromUtf8(TEST_PHRASE);

        private static final long WAIT_INTERVAL = 500;

        private static final int MSG_TEST_PASSED = 0;
        private static final int MSG_TEST_FAILED = 1;

        private TestConnectionListener mListener;

        public void setListener(TestConnectionListener listener) {
            mListener = listener;
        }

        public void start() {
            reset();

            sendEmptyMessageDelayed(MSG_TEST_FAILED, WAIT_INTERVAL);
            send(TEST_PACKET, mBroadcastAddress);
        }

        public boolean isTestPhrase(ByteString data) {
            final String phrase = data.toStringUtf8();
            return TEST_PHRASE.equals(phrase);
        }

        public void passed() {
            sendEmptyMessage(MSG_TEST_PASSED);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            reset();
            if (mListener != null)
                mListener.onTestCompleted(msg.what == MSG_TEST_PASSED ? ConnectionsStatusReciever.STATUS_OK : ConnectionsStatusReciever.STATUS_BLOCKED);
        }

        private void reset() {
            removeMessages(MSG_TEST_FAILED);
            removeMessages(MSG_TEST_PASSED);
        }
    }
}
