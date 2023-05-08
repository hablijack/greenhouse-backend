package de.hablijack.greenhouse.service;

import de.hablijack.greenhouse.webclient.SatelliteClient;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.URI;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

@ApplicationScoped
public class SatelliteService {

  public SatelliteClient createWebClient(String baseUrl) {
    return RestClientBuilder.newBuilder().baseUri(URI.create(baseUrl)).build(SatelliteClient.class);
  }
}
