package de.hablijack.greenhouse.cron;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import de.hablijack.greenhouse.entity.Satelite;
import de.hablijack.greenhouse.webclient.WebClientResource;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.scheduler.Scheduled;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class HealthCheckScheduler {
  @Inject
  WebClientResource webClient;

  @Scheduled(every = "2m", concurrentExecution = SKIP)
  @Transactional
  void sateliteHealthCheck() {
    for (PanacheEntityBase entity : Satelite.listAll()) {
      Satelite satelite = (Satelite) entity;
      Map<String, String> result = webClient.doGETRequest("http://" + satelite.ip + "/health").await().indefinitely();
      satelite.online = result != null;
      satelite.persist();
    }
  }
}
