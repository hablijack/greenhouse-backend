package de.hablijack.greenhouse.api.relay;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hablijack.greenhouse.entity.RelayLog;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logmanager.Level;

@ServerEndpoint("/api/relays/socket")
@ApplicationScoped
public class RelayLogSocket {

  private static final Logger LOGGER = Logger.getLogger(RelayLogSocket.class.getName());

  @Inject
  ManagedExecutor managedExecutor;

  @Inject
  TransactionManager transactionManager;

  @Inject
  RelayLog relayLog;

  @SuppressWarnings("checkstyle:MagicNumber")
  @OnOpen
  public void onOpen(Session session) {
    managedExecutor.submit(() -> {
      ObjectMapper objectMapper = new ObjectMapper();
      try {
        transactionManager.begin();
        String jsonObject = null;
        jsonObject = objectMapper.writeValueAsString(RelayLog.getRecentLog(10));
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
}
