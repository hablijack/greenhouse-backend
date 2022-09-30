package de.hablijack.greenhouse.cron;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import de.hablijack.greenhouse.entity.Satelite;
import de.hablijack.greenhouse.webclient.WebClientResource;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.scheduler.Scheduled;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class HealthCheckScheduler {
  private static final Logger LOGGER = Logger.getLogger(HealthCheckScheduler.class.getName());
  @Inject
  WebClientResource webClient;

  @Scheduled(every = "10m", concurrentExecution = SKIP)
  @Transactional
  void sateliteHealthCheck() {
    for (PanacheEntityBase entity : Satelite.listAll()) {
      Satelite satelite = (Satelite) entity;
      Map<String, String> result = webClient.doGETRequest("http://" + satelite.ip + "/health").await().indefinitely();
      satelite.online = result != null;
      LOGGER.log(Level.INFO, String.valueOf(satelite.online));
      satelite.persist();
    }
  }
}
