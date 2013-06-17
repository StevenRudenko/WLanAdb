package com.wlanadb.service;

import java.net.InterfaceAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.wlanadb.data.ClientProto.Client;
import com.wlanadb.data.LogcatLine;
import com.wlanadb.data.MessageProto.Message;
import com.wlanadb.data.MessageProto.Message.Type;
import com.wlanadb.network.BroadcastServer;
import com.wlanadb.network.P2PClient;
import com.wlanadb.utils.NetworkUtils;
import com.wlanadb.worker.BaseWorker;
import com.wlanadb.worker.LogcatWorker;
import com.wlanadb.worker.LogcatWorker.OnLogMessageListener;

public class WLanAdbController {
  @SuppressWarnings("unused")
  private static final String TAG = WLanAdbController.class.getSimpleName();

  public interface OnWLanAdbControllerEventListener {
    public void onClientListUpdated(ClientManager manager);
    public void onLogUpdated(LogManager manager);
  }

  private final BroadcastServer broadcastServer;
  private final ClientManager clientManager;
  private final LogManager logManager;

  private final Set<OnWLanAdbControllerEventListener> listeners = new HashSet<OnWLanAdbControllerEventListener>();

  private P2PClient p2pClient;

  public WLanAdbController() {
    clientManager = new ClientManager(new OnClientsListChanged() {
      @Override
      public void onClientsListChanged(List<Client> clients) {
        for (OnWLanAdbControllerEventListener listener : listeners) {
          listener.onClientListUpdated(clientManager);
        }
      }
    });

    broadcastServer = new BroadcastServer(clientManager);
    logManager = new LogManager(new OnLogMessageListener() {
      @Override
      public void onLogMessage(LogcatLine message) {
        for (OnWLanAdbControllerEventListener listener : listeners) {
          listener.onLogUpdated(logManager);
        }
      }
    });
  }

  public synchronized boolean start() {
    final InterfaceAddress network = NetworkUtils.getNetworkAddress();
    return broadcastServer.start(network.getBroadcast(), network.getAddress());
  }

  public synchronized void stop() {
    if (p2pClient != null)
      p2pClient.stop();
    broadcastServer.stop();
  }

  public void addEventsListener(OnWLanAdbControllerEventListener listener) {
    listeners.add(listener);
  }

  public void removeEventsListener(OnWLanAdbControllerEventListener listener) {
    listeners.remove(listener);
  }

  public ClientManager getClientManager() {
    return clientManager;
  }

  public LogManager getLogManager() {
    return logManager;
  }

  public void scan() {
    final Message request = Message.newBuilder().setType(Type.REQEST).build();
    broadcastServer.send(request.toByteString(), null);
  }

  public void startLogging(Client client) {
    final LogcatWorker worker = new LogcatWorker(logManager);
    startWorker(client, worker);
  }

  private void startWorker(Client client, BaseWorker worker) {
    synchronized (this) {
      if (p2pClient != null) {
        p2pClient.stop();
        logManager.reset();
      }

      p2pClient = new P2PClient(client);
      p2pClient.start(worker);
    }
  }
}
