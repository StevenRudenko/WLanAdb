package com.wlanadb.utils;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    /**
     * Calculate the local IP of connection.
     */
    public static InterfaceAddress getNetworkAddress() {
        try {
            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                final NetworkInterface network = interfaces.nextElement();
                // we shouldn't care about loopback addresses
                if (network.isLoopback())
                    continue;

                // if you don't expect the interface to be up you can skip this
                // though it would question the usability of the rest of the code
                if (!network.isUp())
                    continue;
                // skip vmware interfaces
                if (network.getName().startsWith("vmnet"))
                    continue;

                // iterate over the addresses associated with the interface
                final List<InterfaceAddress> addresses = network.getInterfaceAddresses();
                for (InterfaceAddress address : addresses) {
                    final InetAddress broadcast = address.getBroadcast();
                    if (broadcast == null)
                        continue;
                    return address;
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "Fail to get network interfaces list", e);
        }

        return null;
    }

}
