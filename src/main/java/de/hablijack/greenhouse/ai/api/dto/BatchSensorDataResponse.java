package de.hablijack.greenhouse.ai.api.dto;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.Map;

@RegisterForReflection
@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
public class BatchSensorDataResponse {

  public Map<String, AiRecommendationResponse> plants;

  public BatchSensorDataResponse() {
  }

  public BatchSensorDataResponse(Map<String, AiRecommendationResponse> plants) {
    this.plants = plants;
  }
}
