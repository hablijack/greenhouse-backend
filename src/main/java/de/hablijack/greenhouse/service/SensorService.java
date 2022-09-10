package de.hablijack.greenhouse.service;

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
    this.influxDBClient =
        InfluxDBClientFactory.create("http://yggdrasil.fritz.box:8086",
            "SAm8mju6EYr8vGlOBrqYQIMcrgwEID1Gxo8nBhCX4ucBigeM-XuHQSwCeA9xECVbB1rUC5dWIChEnDnVNRn_Yg==".toCharArray(),
            "habel");
  }

  @Override
  public void close() throws Exception {
    this.influxDBClient.close();
  }

  public List<Data> getAllData() {
    String temperatureByTimeQuery = Flux.from("greenhouse").range(-14L, ChronoUnit.DAYS).toString();
    QueryApi queryApi = influxDBClient.getQueryApi();
    return queryApi.query(temperatureByTimeQuery, Data.class);
  }
}
