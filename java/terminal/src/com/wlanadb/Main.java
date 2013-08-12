package com.wlanadb;

import com.wlanadb.service.WLanAdbController;

public class Main {

    public static void main(String[] args) {
        final WLanAdbController service = new WLanAdbController();
        final int result = service.start();

        if (result == WLanAdbController.OK) {
            System.out.println("Hello World!");
        } else if (result == WLanAdbController.ERROR_UDP_ADDRESS_USED) {
            System.err.println("WLanAdb: WARNING: It seems you have another application instance launched. Please close it first and try again.");
        } else {
            System.err.println("WLanAdb: WARNING: It seems you are not connected to network. Please check you connection and try again");
        }
    }

    private void startCommand() {

    }
}
