package de.hablijack.greenhouse.ai.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

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
  }
}
