package de.hablijack.greenhouse.api.history;

import de.hablijack.greenhouse.api.pojo.ChartjsData;
import de.hablijack.greenhouse.api.pojo.ChartjsDataset;
import de.hablijack.greenhouse.entity.Measurement;
import de.hablijack.greenhouse.entity.Sensor;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/backend")
public class HistoryResource {

  private final int BORDER_WIDTH = 6;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("history/air/temperatures")
  @SuppressFBWarnings(value = "", justification = "Security is another Epic and on TODO")
  public List<ChartjsDataset> getAirtemperatureHistory(@RestQuery String timerange) {
    List<ChartjsDataset> datalist = new ArrayList<>();
    Sensor airTempInsideSensor = Sensor.findByIdentifier("air_temp_inside");
    List<Measurement> insideMeasurements = airTempInsideSensor.findMeasurementsWithinTimeRange(timerange);
    ChartjsDataset insideData = new ChartjsDataset();
    insideData.setBackgroundColor("rgba(92,127,173,0.1)");
    insideData.setBorderColor("rgba(92,127,173,1)");
    insideData.setBorderWidth(this.BORDER_WIDTH);
    insideData.setFill(true);
    insideData.setLabel("Lufttemperatur innen");
    insideData.setData(this.extractDataFromMeasurements(insideMeasurements));
    datalist.add(insideData);

    Sensor airTempOutsideSensor = Sensor.findByIdentifier("air_temp_outside");
    List<Measurement> outsideMeasurements = airTempOutsideSensor.findMeasurementsWithinTimeRange(timerange);
    ChartjsDataset outsideData = new ChartjsDataset();
    outsideData.setBackgroundColor("rgba(92,168,173,.1)");
    outsideData.setBorderColor("rgba(92,168,173,1)");
    outsideData.setBorderWidth(this.BORDER_WIDTH);
    outsideData.setFill(true);
    outsideData.setLabel("Lufttemperatur außen");
    outsideData.setData(this.extractDataFromMeasurements(outsideMeasurements));
    datalist.add(outsideData);

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
