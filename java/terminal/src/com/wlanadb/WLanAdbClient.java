package com.wlanadb;

import com.wlanadb.data.ClientProto;
import com.wlanadb.data.CommandProto;
import com.wlanadb.data.LogcatLine;
import com.wlanadb.service.ClientManager;
import com.wlanadb.service.LogManager;
import com.wlanadb.service.WLanAdbController;
import com.wlanadb.worker.LogcatWorker;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: steven
 * Date: 8/16/13
 * Time: 8:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class WLanAdbClient implements WLanAdbController.OnWLanAdbControllerEventListener, LogcatWorker.OnLogMessageListener {

    private static final String COMMAND_HELP = "help";
    private static final String COMMAND_DEVICES = "devices";
    private static final String COMMAND_LOGCAT = "logcat";
    private static final String COMMAND_PUSH = "push";
    private static final String COMMAND_INSTALL = "install";
    private static final String COMMAND_VERSION = "version";
    private static final String PARAM_SERIAL_NUMBER = "-s";
    private static final String PARAM_CLEAR_STYLE = "--no-style";
    private static final String PARAM_SILENT_MODE = "--silent";

    private final WLanAdbController service = new WLanAdbController();
    private CommandProto.Command command;

    public WLanAdbClient() {
    }

    public boolean start() {
        final int result = service.start();
        if (result == WLanAdbController.ERROR_UDP_ADDRESS_USED) {
            System.err.println("WLanAdb: WARNING: It seems you have another application instance launched. Please close it first and try again.");
            return false;
        } else if (result == WLanAdbController.ERROR_NOT_CONNECTED) {
            System.err.println("WLanAdb: WARNING: It seems you are not connected to network. Please check you connection and try again");
            return false;
        }

        service.addEventsListener(this);

        service.scan();

        synchronized (service) {
            try {
                service.wait();
            } catch (InterruptedException ignore) {
            }
        }

        return true;
    }

    public void stop() {
        service.stop();
    }

    public boolean parseCommand(String[] args) {
        if (args.length < 1)  {
            printHelp();
            return false;
        }

        final String commandArg = args[0];

        if (COMMAND_DEVICES.equals(commandArg)) {
            return true;
        } else if (COMMAND_HELP.equals(commandArg)) {
            printHelp();
            return true;
        } else if (COMMAND_LOGCAT.equals(commandArg)) {
            command = CommandProto.Command.newBuilder().setCommand(commandArg).build();
        } else {
            return false;
        }


        return true;
    }

    @Override
    public void onClientListUpdated(ClientManager manager) {
        service.removeEventsListener(this);
        service.stop();

        final List<ClientProto.Client> clients = manager.getClients();
        final int count = clients.size();
        if (count == 0) {
            System.out.println("There is no any device found.");
        } else if (count == 1) {
            startCommand(clients.get(0));
        } else {
            System.out.println("" + count + " device(s) found:");
            for (int i=0; i<count; ++i) {
                final ClientProto.Client client = clients.get(i);
                System.out.println("" + (i + 1) + ") " + client.getId() + "\t\t" + client.getName() + " - " + client.getModel() + " (" + client.getFirmware() + ")");
            }
        }

        synchronized (service) {
            service.notify();
        }
    }

    @Override
    public void onLogUpdated(LogManager manager) {
        System.out.println(manager.getLogs().length);
        //To change body of implemented methods use File | Settings | File Templates.
    }


    public void startCommand(ClientProto.Client client) {
        System.out.println("Starting command: " + command.getCommand());
        service.startLogging(client, this);

        synchronized (service) {
            try {
                service.wait();
            } catch (InterruptedException ignore) {
            }
        }
    }

    @Override
    public void onLogMessage(LogcatLine message) {
        System.out.println(message.full);
    }

    private void printHelp() {

    }

    private void printVersion() {

    }
}
