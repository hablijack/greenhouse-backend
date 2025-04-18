package de.hablijack.greenhouse.api.sensor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hablijack.greenhouse.service.SensorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.util.logging.Logger;
import org.jboss.logmanager.Level;

@ServerEndpoint("/api/socket/sensors/measurements")
@ApplicationScoped
public class MeasurementSocket {

  private static final Logger LOGGER = Logger.getLogger(MeasurementSocket.class.getName());

  @Inject
  SensorService sensorService;

  @OnOpen
  @Transactional
  public void onOpen(Session session) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    String jsonObject = objectMapper.writeValueAsString(sensorService.getCurrentSensorValues());
    session.getAsyncRemote().sendObject(jsonObject, result -> {
      if (result.getException() != null) {
        LOGGER.log(Level.ERROR, "Unable to send message! " + result.getException());
      }
    });
  }
}
