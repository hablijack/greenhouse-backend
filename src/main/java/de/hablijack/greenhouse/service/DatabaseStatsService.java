package de.hablijack.greenhouse.service;

import de.hablijack.greenhouse.api.pojo.TableSize;
import java.math.BigInteger;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@SuppressWarnings("checkstyle:LineLength")
@ApplicationScoped
public class DatabaseStatsService {
  @Inject
  EntityManager entityManager;
  private static final String DB_SIZE_QUERY = "SELECT pg_table_size('greenhouse.measurement') as measurementSizeByte, pg_table_size('greenhouse.relay_log') as relayLogSizeByte ; ";
  public TableSize getCurrentDatabaseSize() {
    Query query = entityManager.createNativeQuery(DB_SIZE_QUERY);
    List<Object[]> results = query.getResultList();
    TableSize tableSize = new TableSize();
    tableSize.setMeasurementSizeByte((BigInteger) results.get(0)[0]);
    tableSize.setRelayLogSizeByte((BigInteger) results.get(0)[1]);
    return tableSize;
  }
}
