package de.hablijack.greenhouse.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;
import static jakarta.transaction.Transactional.TxType.REQUIRED;

import de.hablijack.greenhouse.entity.Measurement;
import de.hablijack.greenhouse.entity.RelayLog;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CleanupScheduler {
  @Scheduled(every = "12h", concurrentExecution = SKIP)
  @Transactional(REQUIRED)
  void cleanup() {
    RelayLog.cleanupOldEntries();
    Measurement.cleanupOldEntries();
  }
}
