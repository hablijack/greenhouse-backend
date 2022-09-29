package de.hablijack.greenhouse.webclient;

import io.smallrye.mutiny.Uni;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.resteasy.reactive.client.api.QuarkusRestClientProperties;

@ApplicationScoped
public class WebClientResource {

  private static final long CONNECT_TIMEOUT = 25;
  private static final long READ_TIMEOUT = 1000;
  private static final long CONNECTION_TTL = 25000;
  private static final long CONNECTION_POOL_SIZE = 200;
  Map<String, WebClient> baseUrlToClient = new ConcurrentHashMap<>();

  private static String[] splitUrl(String fullUrl) {
    try {
      var url = new URL(fullUrl);
      String baseUrl = StringUtils.splitByWholeSeparator(fullUrl, url.getPath())[0];
      String relativePath = StringUtils.removeStart(fullUrl.replace(baseUrl, ""), "/");
      return new String[] {baseUrl, relativePath};
    } catch (ArrayIndexOutOfBoundsException | MalformedURLException e) {
      return new String[] {};
    }
  }

  public Uni<Map<String, String>> doGETRequest(String fullUrl) {
    return Uni.createFrom().item(fullUrl)
        .flatMap(url -> {
          String[] baseAndRelative = splitUrl(fullUrl);
          return registerService(baseAndRelative[0]).getByEndpoint(baseAndRelative[1]);
        })
        .onFailure().recoverWithNull();
  }

  private WebClient registerService(String baseUrl) {
    return baseUrlToClient.computeIfAbsent(baseUrl, key ->
        RestClientBuilder.newBuilder()
            .baseUri(URI.create(key))
            .followRedirects(true)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .property(QuarkusRestClientProperties.CONNECTION_TTL, CONNECTION_TTL)
            .property(QuarkusRestClientProperties.CONNECTION_POOL_SIZE, CONNECTION_POOL_SIZE)
            .property(QuarkusRestClientProperties.NAME, "my-single-client")
            .property(QuarkusRestClientProperties.SHARED, true)
            .build(WebClient.class)
    );
  }
}
