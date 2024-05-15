package de.hablijack.greenhouse.service;

import de.hablijack.greenhouse.entity.CameraPicture;
import de.hablijack.greenhouse.entity.Satellite;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class SatelliteService {

  public static final int CAMERA_SNAPSHOT_WAIT_TIME = 10;
  private static final Logger LOGGER = Logger.getLogger(SatelliteService.class.getName());
  private static final Long CONNECT_TIMEOUT = 10000L;
  private static final Long READ_TIMEOUT = 10000L;
  private static final long MIN_FILE_SIZE = 10000L;
  @RestClient
  SatelliteClient satelliteClient;

  public SatelliteClient createSatelliteClient(String ip) throws MalformedURLException {
    return RestClientBuilder.newBuilder()
        .baseUrl(new URL("http://" + ip))
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
        .build(SatelliteClient.class);
  }

  @SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
  public boolean takeCameraSnapshot() {
    Satellite greenhouseCamera = Satellite.findByIdentifier("greenhouse_cam");
    if (greenhouseCamera != null && greenhouseCamera.online) {
      try {
        satelliteClient = createSatelliteClient(greenhouseCamera.ip);
        String pictureResponse = satelliteClient.takePicture();
        if (!pictureResponse.equals("Taking Photo")) {
          LOGGER.warning("Could not take new picture from greenhouse_cam");
          return false;
        } else {
          return true;
        }
      } catch (Exception error) {
        LOGGER.warning(error.getMessage());
        return false;
      }
    }
    return false;
  }

  @SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE",
      "REC_CATCH_EXCEPTION"})
  @Transactional
  public boolean savePictureToDatabase() {
    Satellite greenhouseCamera = Satellite.findByIdentifier("greenhouse_cam");
    if (greenhouseCamera != null && greenhouseCamera.online) {
      CameraPicture picture = CameraPicture.findExistingOrCreteNew();
      try {
        satelliteClient = createSatelliteClient(greenhouseCamera.ip);
      } catch (MalformedURLException error) {
        LOGGER.warning(error.getMessage());
        return false;
      }
      try {
        File file = satelliteClient.savePicture();
        if (file.length() < MIN_FILE_SIZE) {
          LOGGER.warning("Picture not saved! Image is too small...");
          return false;
        }
        try (FileInputStream readStream = new FileInputStream(file)) {
          picture.imageByte = readStream.readAllBytes();
          picture.timestamp = new Date();
          picture.persist();
          return true;
        } catch (FileNotFoundException error) {
          LOGGER.warning(error.getMessage());
        } catch (IOException error) {
          LOGGER.warning(error.getMessage());
          return false;
        }
      } catch (Exception error) {
        LOGGER.warning(error.getMessage());
        return false;
      }
    }
    return false;
  }

}
