package de.hablijack.greenhouse.api.relay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hablijack.greenhouse.entity.RelayLog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logmanager.Level;

@ServerEndpoint("/backend/socket/relays/{userid}")
@ApplicationScoped
public class RelayLogSocket {

  private static final Logger LOGGER = Logger.getLogger(RelayLogSocket.class.getName());

  Map<String, Session> sessions = new ConcurrentHashMap<>();

  @Inject
  ManagedExecutor managedExecutor;

  @Inject
  TransactionManager transactionManager;

  @OnClose
  public void onClose(Session session, @PathParam("userid") String userid) {
    sessions.remove(userid);
  }

  @SuppressWarnings("checkstyle:MagicNumber")
  @OnOpen
  @Transactional
  public void onOpen(Session session, @PathParam("userid") String userid) throws JsonProcessingException {
    sessions.put(userid, session);

    ObjectMapper objectMapper = new ObjectMapper();
    String jsonObject = objectMapper.writeValueAsString(RelayLog.getRecentLog(30));
    session.getAsyncRemote().sendObject(jsonObject, result -> {
      if (result.getException() != null) {
        LOGGER.log(Level.ERROR, "Unable to send message! " + result.getException().getMessage());
      }
    });
  }

  @OnMessage
  public void onMessage(String message, @PathParam("userid") String userid) {
    broadcast(message);
  }

  private void broadcast(String message) {
    sessions.values().forEach(s -> {
      s.getAsyncRemote().sendObject(message, result -> {
        if (result.getException() != null) {
          LOGGER.log(Level.ERROR, "Unable to send message! " + result.getException().getMessage());
        }
      });
    });
  }
}
