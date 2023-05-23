package de.hablijack.greenhouse.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import de.hablijack.greenhouse.entity.Satellite;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import de.hablijack.greenhouse.webclient.TelegramClient;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.transaction.Transactional;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class HealthCheckScheduler {

  private static final Logger LOGGER = Logger.getLogger(HealthCheckScheduler.class.getName());

  @Inject
  @RestClient
  TelegramClient telegramClient;

  @RestClient
  SatelliteClient satelliteClient;

  @ConfigProperty(name = "telegram.bot.token")
  String botToken;
  @ConfigProperty(name = "telegram.bot.chatid")
  String chatId;

  @Scheduled(every = "1m", concurrentExecution = SKIP)
  @Transactional
  void sateliteHealthCheck() throws MalformedURLException {
    for (PanacheEntityBase entity : Satellite.listAll()) {
      Satellite satellite = (Satellite) entity;
      boolean alreadyOffline = !satellite.online;
      satelliteClient = RestClientBuilder.newBuilder().baseUrl(
          new URL("http://" + satellite.ip)
      ).build(SatelliteClient.class);
      try {
        JsonObject result = satelliteClient.healthcheck();
        String status = ((JsonString) result.get("status")).getString();
        satellite.online = result != null && status.equals("ok");
      } catch (Exception error) {
        if (!alreadyOffline) {
          LOGGER.warning(error.getMessage());
          telegramClient.sendMessage(botToken, chatId,
              "Konnte den Satelliten nicht erreichen! \r\n\r\n"
                  + satellite.name + "\r\n\r\n"
                  + error.getMessage());
        }
        satellite.online = false;
      }
      satellite.persist();
    }
  }
}
