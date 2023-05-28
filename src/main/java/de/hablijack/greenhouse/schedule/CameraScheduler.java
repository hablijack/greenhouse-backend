package de.hablijack.greenhouse.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import de.hablijack.greenhouse.entity.CameraPicture;
import de.hablijack.greenhouse.entity.Satellite;
import de.hablijack.greenhouse.service.SatelliteService;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.logging.Logger;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class CameraScheduler {

  private static final Logger LOGGER = Logger.getLogger(CameraScheduler.class.getName());

  @RestClient
  SatelliteClient satelliteClient;
  @Inject
  SatelliteService satelliteService;

  @SuppressFBWarnings("CRLF_INJECTION_LOGS")
  @Scheduled(every = "20m", concurrentExecution = SKIP)
  void takePicture() {
    Satellite greenhouseCamera = Satellite.findByIdentifier("greenhouse_cam");
    if (greenhouseCamera != null && greenhouseCamera.online) {
      try {
        satelliteClient = satelliteService.createSatelliteClient(greenhouseCamera.ip);
        String pictureResponse = satelliteClient.takePicture();
        if (!pictureResponse.equals("Taking Photo")) {
          LOGGER.warning("Could not take new picture from greenhouse_cam");
        }
      } catch (Exception error) {
        LOGGER.warning(error.getMessage());
      }
    }
  }

  @SuppressFBWarnings("CRLF_INJECTION_LOGS")
  @Scheduled(every = "40m", concurrentExecution = SKIP)
  @Transactional
  void savePictureToDatabase() {
    Satellite greenhouseCamera = Satellite.findByIdentifier("greenhouse_cam");
    if (greenhouseCamera != null && greenhouseCamera.online) {
      try {
        satelliteClient = satelliteService.createSatelliteClient(greenhouseCamera.ip);
        File file = satelliteClient.savePicture();
        CameraPicture picture = CameraPicture.findExistingOrCreteNew();
        picture.imageByte = new FileInputStream(file).readAllBytes();
        picture.timestamp = new Date();
        picture.persist();
      } catch (Exception error) {
        LOGGER.warning(error.getMessage());
      }
    }
  }
}
