package de.hablijack.greenhouse.api.database;

import de.hablijack.greenhouse.api.pojo.TableSize;
import de.hablijack.greenhouse.service.DatabaseStatsService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/backend")
public class StatsResource {

  @Inject
  DatabaseStatsService databaseStatsService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/database/stats")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public TableSize getDatabaseStatistics() {
    return databaseStatsService.getCurrentDatabaseSize();
  }

}
