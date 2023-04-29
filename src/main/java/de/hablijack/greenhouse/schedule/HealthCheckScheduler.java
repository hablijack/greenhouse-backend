package de.hablijack.greenhouse.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import de.hablijack.greenhouse.entity.Satellite;
import de.hablijack.greenhouse.webclient.WebClientResource;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Map;

@ApplicationScoped
public class HealthCheckScheduler {
  @Inject
  WebClientResource webClient;

  @Scheduled(every = "10m", concurrentExecution = SKIP)
  @Transactional
  void sateliteHealthCheck() {
    for (PanacheEntityBase entity : Satellite.listAll()) {
      Satellite satellite = (Satellite) entity;
      Map<String, String> result = webClient.doGETRequest("http://" + satellite.ip + "/health").await().indefinitely();
      satellite.online = result != null;
      satellite.persist();
    }
  }
}
