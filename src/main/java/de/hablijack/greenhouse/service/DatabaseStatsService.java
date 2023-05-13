package de.hablijack.greenhouse.service;

import de.hablijack.greenhouse.api.pojo.TableSize;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@SuppressWarnings("checkstyle:LineLength")
@ApplicationScoped
public class DatabaseStatsService {
  private static final String DB_SIZE_QUERY =
      "SELECT pg_table_size('greenhouse.measurement') as measurementSizeByte, pg_table_size('greenhouse.relay_log') as relayLogSizeByte";
  @Inject
  EntityManager entityManager;

  public TableSize getCurrentDatabaseSize() {
    Query query = entityManager.createNativeQuery(DB_SIZE_QUERY);
    Object[] results = (Object[]) query.getSingleResult();
    TableSize tableSize = new TableSize();
    if (results != null && results.length > 0) {
      tableSize.setMeasurementSizeByte((Long) results[0]);
      tableSize.setRelayLogSizeByte((Long) results[1]);
    }
    return tableSize;
  }
}
