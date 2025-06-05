package de.hablijack.greenhouse.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;
import static jakarta.transaction.Transactional.TxType.REQUIRES_NEW;

import de.hablijack.greenhouse.entity.Measurement;
import de.hablijack.greenhouse.entity.RelayLog;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.logging.Logger;

@ApplicationScoped
public class CleanupScheduler {

  private static final Logger LOGGER = Logger.getLogger(CleanupScheduler.class.getName());
  private static final int TRANSACTION_TIMEOUT = 120;

  @Scheduled(every = "3h", concurrentExecution = SKIP)
  @Transactional(REQUIRES_NEW)
  void cleanup() {
    RelayLog.cleanupOldEntries();
    Measurement.cleanupOldEntries();
  }
}
