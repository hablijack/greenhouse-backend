package de.hablijack.greenhouse.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import de.hablijack.greenhouse.entity.Measurement;
import de.hablijack.greenhouse.entity.Satellite;
import de.hablijack.greenhouse.entity.Sensor;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import de.hablijack.greenhouse.webclient.TelegramClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.transaction.Transactional;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class MeasurementScheduler {

  static final Double HUNDRED_PERCENT_VALUE = 100.0;
  static final Double ZERO_PERCENT_VALUE = 0.0;


  private static final Logger LOGGER = Logger.getLogger(MeasurementScheduler.class.getName());
  @Inject
  @RestClient
  TelegramClient telegramClient;
  @RestClient
  SatelliteClient satelliteClient;
  @ConfigProperty(name = "telegram.bot.token")
  String botToken;
  @ConfigProperty(name = "telegram.bot.chatid")
  String chatId;

  @Scheduled(every = "5m", concurrentExecution = SKIP)
  @SuppressFBWarnings(value = {"DLS_DEAD_LOCAL_STORE", "CRLF_INJECTION_LOGS"})
  @Transactional
  void requestMeasurements() throws MalformedURLException {
    Satellite greenhouseControl = Satellite.findByIdentifier("greenhouse_control");
    if (!greenhouseControl.online) {
      return;
    }

    satelliteClient = RestClientBuilder.newBuilder().baseUrl(
        new URL("http://" + greenhouseControl.ip)
    ).build(SatelliteClient.class);
    try {
      JsonObject currentValues = satelliteClient.getMeasurements();
      for (Sensor sensor : Sensor.<Sensor>listAll()) {
        if (currentValues.containsKey(sensor.identifier)) {
          Measurement measurement = new Measurement();
          measurement.sensor = sensor;
          JsonValue value = currentValues.get(sensor.identifier);
          if (value.getValueType() == JsonValue.ValueType.STRING) {
            String measuredValue = ((JsonString) value).getString();
            if (measuredValue.equals("wet")) {
              measurement.value = HUNDRED_PERCENT_VALUE;
            } else {
              measurement.value = ZERO_PERCENT_VALUE;
            }
          } else if (value.getValueType() == JsonValue.ValueType.NUMBER) {
            measurement.value = ((JsonNumber) value).doubleValue();
          }
        }
      }
    } catch (Exception error) {
      LOGGER.warning(error.getMessage());
      telegramClient.sendMessage(botToken, chatId,
          "Konnte die Sensorwerte nicht abholen! \r\n\r\n"
              + greenhouseControl.name + "\r\n\r\n"
              + error.getMessage());
    }
  }
}
