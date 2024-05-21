package de.hablijack.greenhouse.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import de.hablijack.greenhouse.entity.Satellite;
import de.hablijack.greenhouse.service.SatelliteService;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import java.util.logging.Logger;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class HealthCheckScheduler {

  private static final Logger LOGGER = Logger.getLogger(HealthCheckScheduler.class.getName());

  @RestClient
  SatelliteClient satelliteClient;
  @Inject
  SatelliteService satelliteService;
  private static final int TRANSACTION_TIMEOUT = 70;

  @SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS", "REC_CATCH_EXCEPTION"})
  @Scheduled(every = "1m", concurrentExecution = SKIP)
  void sateliteHealthCheck() {

    QuarkusTransaction.begin(QuarkusTransaction.beginOptions().timeout(TRANSACTION_TIMEOUT));
    try {
      for (PanacheEntityBase entity : Satellite.listAll()) {
        Satellite satellite = (Satellite) entity;
        boolean alreadyOffline = !satellite.online;
        try {
          satelliteClient = satelliteService.createSatelliteClient(satellite.ip);
          JsonObject result = satelliteClient.healthcheck();
          String status = ((JsonString) result.get("status")).getString();
          satellite.online = status.equals("ok");
        } catch (Exception error) {
          if (!alreadyOffline) {
            LOGGER.warning(error.getMessage());
          }
          satellite.online = false;
        }
        satellite.persist();
      }
    } catch (RuntimeException exception) {
      LOGGER.warning("ERROR on HealthCheck Scheduler: " + exception.getMessage());
    } finally {
      QuarkusTransaction.commit();
    }
  }
}
