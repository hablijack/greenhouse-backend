package de.hablijack.greenhouse.api.planting;

import de.hablijack.greenhouse.api.pojo.PlantingTrack;
import de.hablijack.greenhouse.api.pojo.PlantingTrack.RelayInfo;
import de.hablijack.greenhouse.api.pojo.PlantingTrack.SensorInfo;
import de.hablijack.greenhouse.entity.Measurement;
import de.hablijack.greenhouse.entity.Relay;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/api/rest")
public class PlantingTrackResource {

  private static final Pattern LINE_PATTERN = Pattern.compile("line(\\d+)");

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/planting-tracks")
  public List<PlantingTrack> getAllPlantingTracks() {
    List<PlantingTrack> plantingTracks = new ArrayList<>();
    List<Relay> waterRelays = Relay.listAllWaterRelays();

    if (waterRelays == null) {
      return plantingTracks;
    }

    for (Relay relay : waterRelays) {
      int lineNumber = extractLineNumber(relay.identifier);
      if (lineNumber == -1) {
        continue;
      }

      RelayInfo relayInfo = new RelayInfo(relay.id, relay.identifier, relay.value);

      SensorInfo sensorInfo = null;
      if (relay.conditionTrigger != null && relay.conditionTrigger.triggerSensor != null) {
        Double currentValue = null;
        Measurement currentMeasurement = relay.conditionTrigger.triggerSensor.findCurrentMeasurement();
        if (currentMeasurement != null) {
          currentValue = currentMeasurement.value;
        }
        sensorInfo = new SensorInfo(
            relay.conditionTrigger.triggerSensor.identifier,
            currentValue,
            relay.conditionTrigger.triggerSensor.minAlarmValue,
            relay.conditionTrigger.triggerSensor.maxAlarmValue,
            relay.conditionTrigger.triggerSensor.unit
        );
      }

      PlantingTrack track = new PlantingTrack(lineNumber, relay.target, relayInfo, sensorInfo);
      plantingTracks.add(track);
    }

    plantingTracks.sort((a, b) -> Integer.compare(a.getLineNumber(), b.getLineNumber()));
    return plantingTracks;
  }

  private int extractLineNumber(String identifier) {
    Matcher matcher = LINE_PATTERN.matcher(identifier.toLowerCase(Locale.ROOT));
    if (matcher.find()) {
      return Integer.parseInt(matcher.group(1));
    }
    return -1;
  }
}
