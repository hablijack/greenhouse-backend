package de.hablijack.greenhouse.service;

import de.hablijack.greenhouse.entity.Satellite;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class SatelliteService {

  private static final Logger LOGGER = Logger.getLogger(SatelliteService.class.getName());
  private static final Long CONNECT_TIMEOUT = 10000L;
  private static final Long READ_TIMEOUT = 10000L;

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
  public void takeCameraSnapshot() {
    Satellite greenhouseCamera = Satellite.findByIdentifier("greenhouse_cam");
    if (greenhouseCamera != null && greenhouseCamera.online) {
      try {
        satelliteClient = createSatelliteClient(greenhouseCamera.ip);
        String pictureResponse = satelliteClient.takePicture();
        if (!pictureResponse.equals("Taking Photo")) {
          LOGGER.warning("Could not take new picture from greenhouse_cam");
        }
      } catch (Exception error) {
        LOGGER.warning(error.getMessage());
      }
    }
  }
}
