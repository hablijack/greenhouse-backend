package de.hablijack.greenhouse.schedule;

import de.hablijack.greenhouse.service.SatelliteService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

@ApplicationScoped
public class CameraScheduler {

  private static final Logger LOGGER = Logger.getLogger(CameraScheduler.class.getName());

  private static final int TRANSACTION_TIMEOUT = 40;

  @Inject
  SatelliteService satelliteService;

  @SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS"})
  @Scheduled(every = "2m", concurrentExecution = SKIP)
  void takePicture() {
    QuarkusTransaction.begin(QuarkusTransaction.beginOptions().timeout(TRANSACTION_TIMEOUT));
    try {
      satelliteService.takeCameraSnapshot();
      TimeUnit.SECONDS.sleep(SatelliteService.CAMERA_SNAPSHOT_WAIT_TIME);
      satelliteService.savePictureToDatabase();
    } catch (Exception exception) {
      LOGGER.warning("ERROR on takePicture Scheduler: " + exception.getMessage());
    } finally {
      QuarkusTransaction.commit();
    }
  }
}
