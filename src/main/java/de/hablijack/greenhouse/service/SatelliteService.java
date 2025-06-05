package de.hablijack.greenhouse.service;

import de.hablijack.greenhouse.entity.CameraPicture;
import de.hablijack.greenhouse.entity.Satellite;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import io.vertx.core.http.HttpClientOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class SatelliteService {

  public static final int CAMERA_SNAPSHOT_WAIT_TIME = 7;
  private static final Logger LOGGER = Logger.getLogger(SatelliteService.class.getName());
  private static final int CONNECT_TIMEOUT = 5000;
  private static final int READ_TIMEOUT = 5000;

  @RestClient
  SatelliteClient satelliteClient;

  public SatelliteClient createSatelliteClient(String ip) throws URISyntaxException, MalformedURLException {
    HttpClientOptions options = new HttpClientOptions();
    options.setConnectTimeout(CONNECT_TIMEOUT);
    options.setReadIdleTimeout(READ_TIMEOUT);
    options.setIdleTimeoutUnit(TimeUnit.MILLISECONDS);
    options.setSslHandshakeTimeout(CONNECT_TIMEOUT);
    options.setSslHandshakeTimeoutUnit(TimeUnit.MILLISECONDS);
    options.setWriteIdleTimeout(READ_TIMEOUT);
    options.setIdleTimeout(READ_TIMEOUT);
    options.setHttp2KeepAliveTimeout(READ_TIMEOUT);
    return QuarkusRestClientBuilder.newBuilder()
        .baseUri(new URI("http://" + ip))
        .httpClientOptions(options)
        .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
        .connectTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
        .build(SatelliteClient.class);
  }

  @SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"})
  @Transactional
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

  @Transactional
  public void savePictureToDatabase() throws IOException, URISyntaxException {
    Satellite greenhouseCamera = Satellite.findByIdentifier("greenhouse_cam");
    if (greenhouseCamera != null && greenhouseCamera.online) {
      CameraPicture picture = CameraPicture.findExistingOrCreteNew();
      satelliteClient = createSatelliteClient(greenhouseCamera.ip);
      File response = satelliteClient.requestPhoto();
      try (FileInputStream readStream = new FileInputStream(response)) {
        picture.imageByte = readStream.readAllBytes();
      }
      picture.timestamp = new Date();
      if (!picture.isPersistent()) {
        picture.persist();
      }
    }
  }
}
