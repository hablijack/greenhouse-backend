package de.hablijack.greenhouse.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import de.hablijack.greenhouse.entity.Measurement;
import de.hablijack.greenhouse.entity.Satellite;
import de.hablijack.greenhouse.entity.Sensor;
import de.hablijack.greenhouse.service.SatelliteService;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import java.util.Date;
import java.util.logging.Logger;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class MeasurementScheduler {

  static final Double HUNDRED_PERCENT_VALUE = 100.0;
  static final Double ZERO_PERCENT_VALUE = 0.0;

  private static final Logger LOGGER = Logger.getLogger(MeasurementScheduler.class.getName());

  private static final String LIGHT_RELAY_IDENTIFIER = "relay_line7";
  private static final int TRANSACTION_TIMEOUT = 300;
  @RestClient
  SatelliteClient satelliteClient;
  @Inject
  SatelliteService satelliteService;

  @Scheduled(every = "10m", concurrentExecution = SKIP)
  @SuppressFBWarnings(value = {"DLS_DEAD_LOCAL_STORE", "CRLF_INJECTION_LOGS",
      "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", "REC_CATCH_EXCEPTION"})
  void requestMeasurements() {
    QuarkusTransaction.begin(QuarkusTransaction.beginOptions().timeout(TRANSACTION_TIMEOUT));
    try {
      Satellite greenhouseControl = Satellite.findByIdentifier("greenhouse_control");
      if (greenhouseControl != null && greenhouseControl.online) {
        satelliteClient = satelliteService.createSatelliteClient(greenhouseControl.ip);
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
            if (measurement.value != null) {
              measurement.timestamp = new Date();
              measurement.persist();
            }
          }
        }
      }
    } catch (Exception exception) {
      LOGGER.warning("ERROR on Measurement Scheduler: " + exception.getMessage());
    } finally {
      QuarkusTransaction.commit();
    }
  }
}
