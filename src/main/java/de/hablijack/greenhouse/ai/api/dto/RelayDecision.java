package de.hablijack.greenhouse.ai.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;

@RegisterForReflection
public class RelayDecision {

  @JsonProperty("relays")
  public List<RelayAction> relays;

  @JsonProperty("summary")
  public String summary;

  public RelayDecision() {
  }

  @RegisterForReflection
  public static class RelayAction {
    @JsonProperty("relayId")
    public String relayId;

    @JsonProperty("desiredState")
    public boolean desiredState;

    @JsonProperty("reason")
    public String reason;

    public RelayAction() {
    }

    public RelayAction(String relayId, boolean desiredState, String reason) {
      this.relayId = relayId;
      this.desiredState = desiredState;
      this.reason = reason;
    }
  }
}
