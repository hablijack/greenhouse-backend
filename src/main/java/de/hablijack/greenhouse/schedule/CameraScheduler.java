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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.logging.Logger;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class CameraScheduler {

  private static final Logger LOGGER = Logger.getLogger(CameraScheduler.class.getName());
  private static final long MIN_FILE_SIZE = 195000L;

  @RestClient
  SatelliteClient satelliteClient;
  @Inject
  SatelliteService satelliteService;

  @SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
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

  @SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
  @Scheduled(every = "23m", concurrentExecution = SKIP)
  @Transactional
  void savePictureToDatabase() throws MalformedURLException {
    Satellite greenhouseCamera = Satellite.findByIdentifier("greenhouse_cam");
    if (greenhouseCamera != null && greenhouseCamera.online) {
      satelliteClient = satelliteService.createSatelliteClient(greenhouseCamera.ip);
      File file = satelliteClient.savePicture();
      if (file.length() < MIN_FILE_SIZE) {
        return;
      }
      CameraPicture picture = CameraPicture.findExistingOrCreteNew();
      try (FileInputStream readStream = new FileInputStream(file)) {
        picture.imageByte = readStream.readAllBytes();
        picture.timestamp = new Date();
        picture.persist();
      } catch (FileNotFoundException error) {
        LOGGER.warning(error.getMessage());
      } catch (IOException error) {
        LOGGER.warning(error.getMessage());
      }
    }
  }
}
