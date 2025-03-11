package de.hablijack.greenhouse.service;

import de.hablijack.greenhouse.entity.CameraPicture;
import de.hablijack.greenhouse.entity.Satellite;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@ApplicationScoped
public class SatelliteService {

  public static final int CAMERA_SNAPSHOT_WAIT_TIME = 10;
  private static final Logger LOGGER = Logger.getLogger(SatelliteService.class.getName());
  private static final Long CONNECT_TIMEOUT = 10000L;
  private static final Long READ_TIMEOUT = 9000L;

  @RestClient
  SatelliteClient satelliteClient;

  public SatelliteClient createSatelliteClient(String ip) throws URISyntaxException, MalformedURLException {
    return RestClientBuilder.newBuilder()
        .baseUrl(new URI("http://" + ip).toURL())
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
        .build(SatelliteClient.class);
  }

  @SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
  public void takeCameraSnapshot() throws MalformedURLException, URISyntaxException {
    Satellite greenhouseCamera = Satellite.findByIdentifier("greenhouse_cam");
    if (greenhouseCamera != null && greenhouseCamera.online) {
      satelliteClient = createSatelliteClient(greenhouseCamera.ip);
      String pictureResponse = satelliteClient.takePicture();
      if (!pictureResponse.contains("Taking Photo")) {
        LOGGER.warning("Could not shoot new picture from greenhouse_cam: " + pictureResponse);
      }
    }
  }

  @SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE",
      "REC_CATCH_EXCEPTION"})
  public void savePictureToDatabase() throws IOException, URISyntaxException {
    Satellite greenhouseCamera = Satellite.findByIdentifier("greenhouse_cam");
    if (greenhouseCamera != null && greenhouseCamera.online) {
      CameraPicture picture = CameraPicture.findExistingOrCreteNew();
      satelliteClient = createSatelliteClient(greenhouseCamera.ip);
      File file = satelliteClient.savePicture();
      try (FileInputStream readStream = new FileInputStream(file)) {
        picture.imageByte = readStream.readAllBytes();
        picture.timestamp = new Date();
        picture.persist();
      } catch (Exception exception) {
        LOGGER.warning("Could not save picture to database: " + exception.getMessage());
      }
    }
  }
}
