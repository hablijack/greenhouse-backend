package de.hablijack.greenhouse.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import de.hablijack.greenhouse.entity.Measurement;
import de.hablijack.greenhouse.entity.RelayLog;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

@ApplicationScoped
public class CleanupScheduler {

  private static final Logger LOGGER = Logger.getLogger(CleanupScheduler.class.getName());
  private static final int TRANSACTION_TIMEOUT = 120;

  @SuppressFBWarnings("CRLF_INJECTION_LOGS")
  @Scheduled(every = "3h", concurrentExecution = SKIP)
  void cleanup() {
    QuarkusTransaction.begin(QuarkusTransaction.beginOptions().timeout(TRANSACTION_TIMEOUT));
    try {
      RelayLog.cleanupOldEntries();
      Measurement.cleanupOldEntries();
    } catch (Exception exception) {
      LOGGER.warning("ERROR on claning up old db entries: " + exception.getMessage());
    } finally {
      QuarkusTransaction.commit();
    }
  }
}
