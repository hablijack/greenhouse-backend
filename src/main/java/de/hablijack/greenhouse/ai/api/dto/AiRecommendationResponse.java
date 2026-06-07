package de.hablijack.greenhouse.ai.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;

@RegisterForReflection
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
public class AiRecommendationResponse {

  @JsonProperty("summary")
  public String summary;

  @JsonProperty("recommendations")
  public List<String> recommendations;

  @JsonProperty("urgency")
  public String urgency;

  @JsonProperty("riskAssessment")
  public String riskAssessment;

  @JsonProperty("plantType")
  public String plantType;

  @JsonProperty("analysis")
  public SensorAnalysis analysis;

  public AiRecommendationResponse() {
  }

  public AiRecommendationResponse(String summary, List<String> recommendations, String urgency,
      String riskAssessment, String plantType, SensorAnalysis analysis) {
    this.summary = summary;
    this.recommendations = recommendations;
    this.urgency = urgency;
    this.riskAssessment = riskAssessment;
    this.plantType = plantType;
    this.analysis = analysis;
  }

  @RegisterForReflection
  public static class SensorAnalysis {
    @JsonProperty("temperatureStatus")
    public String temperatureStatus;

    @JsonProperty("humidityStatus")
    public String humidityStatus;

    @JsonProperty("soilMoistureStatus")
    public String soilMoistureStatus;

    @JsonProperty("lightStatus")
    public String lightStatus;

    @JsonProperty("co2Status")
    public String co2Status;

    @JsonProperty("timeOfDay")
    public String timeOfDay;

    @JsonProperty("season")
    public String season;

    @JsonProperty("temperatureTrend")
    public String temperatureTrend;

    @JsonProperty("humidityTrend")
    public String humidityTrend;

    @JsonProperty("soilMoistureTrend")
    public String soilMoistureTrend;

    @JsonProperty("lightTrend")
    public String lightTrend;

    @JsonProperty("co2Trend")
    public String co2Trend;
  }
}
