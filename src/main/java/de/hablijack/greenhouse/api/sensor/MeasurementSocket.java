package de.hablijack.greenhouse.api.sensor;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hablijack.greenhouse.service.SensorService;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.TransactionManager;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logmanager.Level;

@ServerEndpoint("/api/sensors/measurements/socket")
@ApplicationScoped
public class MeasurementSocket {

  private static final Logger LOGGER = Logger.getLogger(MeasurementSocket.class.getName());

  @Inject
  ManagedExecutor managedExecutor;

  @Inject
  TransactionManager transactionManager;

  @Inject
  SensorService sensorService;

  @OnOpen
  public void onOpen(Session session) {
    managedExecutor.submit(() -> {
      ObjectMapper objectMapper = new ObjectMapper();
      try {
        transactionManager.begin();
        String jsonObject = objectMapper.writeValueAsString(sensorService.getCurrentSensorValues());
        transactionManager.commit();
        session.getAsyncRemote().sendObject(jsonObject, result -> {
          if (result.getException() != null) {
            LOGGER.log(Level.ERROR, "Unable to send message: " + result.getException());
          }
        });
      } catch (Exception ex) {
        LOGGER.log(Level.ERROR, "Unable to send message: " + ex.getMessage());
      }
    });
  }
}
