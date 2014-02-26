package com.wlanadb;

import com.wlanadb.service.ClientManager;
import com.wlanadb.service.LogManager;
import com.wlanadb.service.WLanAdbController;

public class Main {

    public static void main(String[] args) {
        final WLanAdbClient client = new WLanAdbClient();
        client.parseCommand(args);
        if (!client.start())
            return;

        client.stop();
    }

}
