package de.hablijack.greenhouse.api.relay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hablijack.greenhouse.entity.RelayLog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
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

@ServerEndpoint("/backend/relays/socket/{userid}")
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
  public void onOpen(Session session, @PathParam("userid") String userid) {
    sessions.put(userid, session);
    managedExecutor.submit(() -> {
      ObjectMapper objectMapper = new ObjectMapper();
      try {
        transactionManager.begin();
        String jsonObject = objectMapper.writeValueAsString(RelayLog.getRecentLog(30));
        transactionManager.commit();
        session.getAsyncRemote().sendObject(jsonObject, result -> {
          if (result.getException() != null) {
            LOGGER.log(Level.ERROR, "Unable to send message!");
          }
        });
      } catch (NotSupportedException | SystemException | JsonProcessingException | RollbackException
               | HeuristicMixedException | HeuristicRollbackException e) {
        throw new RuntimeException(e);
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
          LOGGER.log(Level.ERROR, "Unable to send message!");
        }
      });
    });
  }
}
