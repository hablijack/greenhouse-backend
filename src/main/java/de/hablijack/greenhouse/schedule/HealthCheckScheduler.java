package de.hablijack.greenhouse.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import de.hablijack.greenhouse.entity.Satellite;
import de.hablijack.greenhouse.service.SatelliteService;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import de.hablijack.greenhouse.webclient.TelegramClient;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.transaction.Transactional;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class HealthCheckScheduler {

  private static final Logger LOGGER = Logger.getLogger(HealthCheckScheduler.class.getName());

  @Inject
  @RestClient
  TelegramClient telegramClient;

  @Inject
  SatelliteService satelliteService;

  @ConfigProperty(name = "telegram.bot.token")
  String botToken;
  @ConfigProperty(name = "telegram.bot.chatid")
  String chatId;

  @Scheduled(every = "1m", concurrentExecution = SKIP)
  @Transactional
  void sateliteHealthCheck() {
    for (PanacheEntityBase entity : Satellite.listAll()) {
      Satellite satellite = (Satellite) entity;
      SatelliteClient satelliteClient = satelliteService.createWebClient(satellite.ip);
      try {
        JsonObject result = satelliteClient.healthcheck();
        satellite.online = result != null && result.get("status").equals("ok");
      } catch (Exception error) {
        LOGGER.warning(error.getMessage());
        telegramClient.sendMessage(botToken, chatId,
            "Konnte den Satelliten nicht erreichen! \r\n\r\n"
                + satellite.name + "\r\n\r\n"
                + error.getMessage());
        satellite.online = false;
      }
      satellite.persist();
    }
  }
}
