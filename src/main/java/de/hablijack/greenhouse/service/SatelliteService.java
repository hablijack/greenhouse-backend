package de.hablijack.greenhouse.service;

import de.hablijack.greenhouse.webclient.SatelliteClient;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

@ApplicationScoped
public class SatelliteService {

  private static final Long CONNECT_TIMEOUT = Long.valueOf(10000);
  private static final Long READ_TIMEOUT = Long.valueOf(10000);

  public SatelliteClient createSatelliteClient(String ip) throws MalformedURLException {
    return RestClientBuilder.newBuilder()
        .baseUrl(new URL("http://" + ip))
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
        .build(SatelliteClient.class);
  }
}
