package eu.hablijack.service.data;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.dsl.Flux;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.xml.crypto.Data;

@ApplicationScoped
public class SensorService implements AutoCloseable {
  private InfluxDBClient influxDBClient;

  @PostConstruct
  private void initializeInfluxDBClient() {
    this.influxDBClient = InfluxDBClientFactory.create("", "token".toCharArray(), "orgId", "bucketId");
  }

  @Override
  public void close() throws Exception {
    this.influxDBClient.close();
  }

  public List<Data> getAllData() {
    String temperatureByTimeQuery = Flux.from("bucketName").range(-14L, ChronoUnit.DAYS).toString();
    QueryApi queryApi = influxDBClient.getQueryApi();
    return queryApi.query(temperatureByTimeQuery, Data.class);
  }
}
