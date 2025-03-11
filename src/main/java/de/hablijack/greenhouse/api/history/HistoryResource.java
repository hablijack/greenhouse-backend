package de.hablijack.greenhouse.api.history;

import de.hablijack.greenhouse.api.pojo.ChartjsData;
import de.hablijack.greenhouse.api.pojo.ChartjsDataset;
import de.hablijack.greenhouse.entity.Measurement;
import de.hablijack.greenhouse.entity.Sensor;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/backend")
public class HistoryResource {

  private static final int BORDER_WIDTH = 4;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("history/air/temperatures")

  public List<ChartjsDataset> getAirtemperatureHistory(@RestQuery String timerange) {
    if (timerange == null) {
      timerange = "week";
    }
    List<ChartjsDataset> datalist = new ArrayList<>();
    Sensor airTempInsideSensor = Sensor.findByIdentifier("air_temp_inside");
    List<Measurement> insideMeasurements = airTempInsideSensor.findMeasurementsWithinTimeRange(timerange);
    ChartjsDataset insideData = new ChartjsDataset();
    insideData.setBackgroundColor("rgba(92,127,173,0.1)");
    insideData.setBorderColor("rgba(92,127,173,1)");
    insideData.setBorderWidth(BORDER_WIDTH);
    insideData.setFill(true);
    insideData.setLabel("Lufttemperatur innen");
    insideData.setData(this.extractDataFromMeasurements(insideMeasurements));
    datalist.add(insideData);

    Sensor airTempOutsideSensor = Sensor.findByIdentifier("air_temp_outside");
    List<Measurement> outsideMeasurements = airTempOutsideSensor.findMeasurementsWithinTimeRange(timerange);
    ChartjsDataset outsideData = new ChartjsDataset();
    outsideData.setBackgroundColor("rgba(92,168,173,.1)");
    outsideData.setBorderColor("rgba(92,168,173,1)");
    outsideData.setBorderWidth(BORDER_WIDTH);
    outsideData.setFill(true);
    outsideData.setLabel("Lufttemperatur außen");
    outsideData.setData(this.extractDataFromMeasurements(outsideMeasurements));
    datalist.add(outsideData);

    return datalist;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("history/air/humidity")
  public List<ChartjsDataset> getAirhumidityHistory(@RestQuery String timerange) {
    if (timerange == null) {
      timerange = "week";
    }
    List<ChartjsDataset> datalist = new ArrayList<>();
    Sensor airHumiditySensor = Sensor.findByIdentifier("air_humidity_inside");
    List<Measurement> insideMeasurements = airHumiditySensor.findMeasurementsWithinTimeRange(timerange);
    ChartjsDataset insideData = new ChartjsDataset();
    insideData.setBackgroundColor("rgba(92,127,173,.1)");
    insideData.setBorderColor("rgba(92,168,173,1)");
    insideData.setBorderWidth(BORDER_WIDTH);
    insideData.setFill(true);
    insideData.setLabel("Luftfeuchtigkeit innen");
    insideData.setData(this.extractDataFromMeasurements(insideMeasurements));
    datalist.add(insideData);
    return datalist;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("history/wifi")
  public List<ChartjsDataset> getWifiStrengthHistory(@QueryParam("timerange") String timerange) {
    if (timerange == null) {
      timerange = "week";
    }
    List<ChartjsDataset> datalist = new ArrayList<>();
    Sensor wifiSensor = Sensor.findByIdentifier("wifi");
    List<Measurement> wifiMeasurements = wifiSensor.findMeasurementsWithinTimeRange(timerange);
    ChartjsDataset wifiData = new ChartjsDataset();
    wifiData.setBackgroundColor("rgba(255,255,60,.1)");
    wifiData.setBorderColor("rgba(255,255,60,1)");
    wifiData.setBorderWidth(BORDER_WIDTH);
    wifiData.setFill(true);
    wifiData.setLabel("Signalstärke");
    wifiData.setData(this.extractDataFromMeasurements(wifiMeasurements));
    datalist.add(wifiData);
    return datalist;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("history/co2")

  public List<ChartjsDataset> getCO2History(@QueryParam("timerange") String timerange) {
    if (timerange == null) {
      timerange = "week";
    }
    List<ChartjsDataset> datalist = new ArrayList<>();
    Sensor co2Sensor = Sensor.findByIdentifier("co2");
    List<Measurement> co2Measurements = co2Sensor.findMeasurementsWithinTimeRange(timerange);
    ChartjsDataset co2Data = new ChartjsDataset();
    co2Data.setBackgroundColor("rgba(0,255,100,.1)");
    co2Data.setBorderColor("rgba(0,255,100,1)");
    co2Data.setBorderWidth(BORDER_WIDTH);
    co2Data.setFill(true);
    co2Data.setLabel("CO2 Gehalt");
    co2Data.setData(this.extractDataFromMeasurements(co2Measurements));
    datalist.add(co2Data);
    return datalist;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("history/brightness")

  public List<ChartjsDataset> getBrightnessHistory(@QueryParam("timerange") String timerange) {
    if (timerange == null) {
      timerange = "week";
    }
    List<ChartjsDataset> datalist = new ArrayList<>();
    Sensor brightnessSensor = Sensor.findByIdentifier("brightness");
    List<Measurement> brightnessMeasurements = brightnessSensor.findMeasurementsWithinTimeRange(timerange);
    ChartjsDataset brightnessData = new ChartjsDataset();
    brightnessData.setBackgroundColor("rgba(0,255,255,.1)");
    brightnessData.setBorderColor("rgba(0,255,255,1)");
    brightnessData.setBorderWidth(BORDER_WIDTH);
    brightnessData.setFill(true);
    brightnessData.setLabel("Helligkeit");
    brightnessData.setData(this.extractDataFromMeasurements(brightnessMeasurements));
    datalist.add(brightnessData);
    return datalist;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("history/battery")

  public List<ChartjsDataset> getBatteryistory(@QueryParam("timerange") String timerange) {
    if (timerange == null) {
      timerange = "week";
    }
    List<ChartjsDataset> datalist = new ArrayList<>();
    Sensor batterySensor = Sensor.findByIdentifier("battery");
    List<Measurement> batteryMeasurements = batterySensor.findMeasurementsWithinTimeRange(timerange);
    ChartjsDataset batteryData = new ChartjsDataset();
    batteryData.setBackgroundColor("rgba(0,0,0,.1)");
    batteryData.setBorderColor("rgba(0,0,0,1)");
    batteryData.setBorderWidth(BORDER_WIDTH);
    batteryData.setFill(true);
    batteryData.setLabel("battery Gehalt");
    batteryData.setData(this.extractDataFromMeasurements(batteryMeasurements));
    datalist.add(batteryData);
    return datalist;
  }

  private List<ChartjsData> extractDataFromMeasurements(List<Measurement> measurements) {
    List<ChartjsData> data = new ArrayList<>();
    for (Measurement measurement : measurements) {
      ChartjsData singleData = new ChartjsData(measurement.timestamp, measurement.value);
      data.add(singleData);
    }
    return data;
  }
}
