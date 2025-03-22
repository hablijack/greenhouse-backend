package de.hablijack.greenhouse.api.database;

import de.hablijack.greenhouse.api.pojo.TableSize;
import de.hablijack.greenhouse.service.DatabaseStatsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/backend/rest")
public class StatsResource {

  @Inject
  DatabaseStatsService databaseStatsService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/database/stats")
  public TableSize getDatabaseStatistics() {
    return databaseStatsService.getCurrentDatabaseSize();
  }

}
