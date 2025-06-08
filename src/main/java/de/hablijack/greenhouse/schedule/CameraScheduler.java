package de.hablijack.greenhouse.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import de.hablijack.greenhouse.service.SatelliteService;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.logging.Logger;

@ApplicationScoped
public class CameraScheduler {

  private static final Logger LOGGER = Logger.getLogger(CameraScheduler.class.getName());

  @Inject
  SatelliteService satelliteService;

  @Scheduled(every = "2m", concurrentExecution = SKIP)
  void takePicture() {
    try {
      satelliteService.savePictureToDatabase();
    } catch (Exception e) {
      LOGGER.warning("Error on taking camera shot: " + e.getMessage());
    }
  }
}
