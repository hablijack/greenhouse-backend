package de.hablijack.greenhouse.api.relay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hablijack.greenhouse.entity.RelayLog;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
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
        String jsonObject = objectMapper.writeValueAsString(RelayLog.getRecentLog(18));
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
