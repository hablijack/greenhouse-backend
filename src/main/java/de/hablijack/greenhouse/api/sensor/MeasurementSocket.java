package de.hablijack.greenhouse.api.sensor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hablijack.greenhouse.service.SensorService;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.jboss.logmanager.Level;

@ServerEndpoint("/api/sensors/measurements/socket")
@ApplicationScoped
public class MeasurementSocket {

  private static final Logger LOGGER = Logger.getLogger(MeasurementSocket.class.getName());

  @Inject
  SensorService sensorService;

  @OnOpen
  public void onOpen(Session session) throws JsonProcessingException {
    try {
      ObjectMapper mapper = new ObjectMapper();
      String value = mapper.writeValueAsString(sensorService.getCurrentSensorValues());
      LOGGER.log(Level.INFO, value);
    } catch (Exception ex) {
      LOGGER.log(Level.ERROR, "Unable to parse json: " + ex.getMessage());
    }
    session.getAsyncRemote().sendObject("test", result -> {
      if (result.getException() != null) {
        LOGGER.log(Level.ERROR, "Unable to send message: " + result.getException());
      }
    });
  }

  @OnClose
  public void onClose(Session session) {
    session.getAsyncRemote().sendObject("Close Session", result -> {
      if (result.getException() != null) {
        LOGGER.log(Level.ERROR, "Unable to send message: " + result.getException());
      }
    });
  }

  @OnError
  public void onError(Session session, Throwable throwable) {
    session.getAsyncRemote().sendObject("Error Session", result -> {
      if (result.getException() != null) {
        LOGGER.log(Level.ERROR, "Unable to send message: " + result.getException());
      }
    });
  }
}
