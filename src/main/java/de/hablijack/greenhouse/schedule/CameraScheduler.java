package de.hablijack.greenhouse.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import de.hablijack.greenhouse.service.SatelliteService;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.net.MalformedURLException;
import java.util.logging.Logger;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class CameraScheduler {

  private static final Logger LOGGER = Logger.getLogger(CameraScheduler.class.getName());

  @RestClient
  SatelliteClient satelliteClient;
  @Inject
  SatelliteService satelliteService;

  @SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
  @Scheduled(every = "20m", concurrentExecution = SKIP)
  void takePicture() {
    boolean success = satelliteService.takeCameraSnapshot();
    if (!success) {
      LOGGER.warning("Could not take snapshot from webcam!");
    }
  }

  @SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
  @Scheduled(every = "23m", concurrentExecution = SKIP)
  @Transactional
  void savePictureToDatabase() throws MalformedURLException {
    boolean success = satelliteService.savePictureToDatabase();
    if (!success) {
      LOGGER.warning("Could not persist webcam image to database!");
    }
  }
}
