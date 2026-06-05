package de.hablijack.greenhouse.ai.api.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.Map;

@RegisterForReflection
public class BatchSensorDataResponse {

  public Map<String, AiRecommendationResponse> plants;

  public BatchSensorDataResponse() {
  }

  public BatchSensorDataResponse(Map<String, AiRecommendationResponse> plants) {
    this.plants = plants;
  }
}
