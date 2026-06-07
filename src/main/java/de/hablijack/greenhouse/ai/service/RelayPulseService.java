package de.hablijack.greenhouse.ai.service;

import de.hablijack.greenhouse.ai.api.dto.RelayDecision.RelayAction;
import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.entity.RelayLog;
import de.hablijack.greenhouse.service.SatelliteService;
import de.hablijack.greenhouse.webclient.SatelliteClient;
import de.hablijack.greenhouse.webclient.TelegramClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class RelayPulseService {

  private static final Logger LOG = LoggerFactory.getLogger(RelayPulseService.class);

  public static final long IRRIGATION_PULSE_MS = 60_000;
  public static final String PULSE_INITIATOR = "AI-PULSE";
  public static final String PULSE_INITIATOR_SHADOW = "AI-PULSE-SHADOW";

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final SatelliteService satelliteService;

  @Inject
  @RestClient
  TelegramClient telegramClient;

  @ConfigProperty(name = "telegram.bot.token")
  String botToken;

  @ConfigProperty(name = "telegram.bot.chatid")
  String chatId;

  @ConfigProperty(name = "ai.shadow.enabled", defaultValue = "false")
  boolean shadowEnabled;

  public RelayPulseService(SatelliteService satelliteService) {
    this.satelliteService = satelliteService;
  }

  public PulsePlan planPulse(RelayAction action, Relay relay) {
    long durationMs = getPulseDuration(action, relay);
    return new PulsePlan(action, relay, durationMs, shadowEnabled);
  }

  public void executePulse(PulsePlan plan) {
    if (plan == null || !plan.action.desiredState) {
      return;
    }

    if (plan.shadow) {
      LOG.info("[SHADOW] Would pulse {} ({}) for {}ms",
          plan.relay.identifier, plan.relay.name, plan.durationMs);
      return;
    }

    LOG.info("Pulsing {} ({}) for {}ms", plan.relay.identifier, plan.relay.name, plan.durationMs);

    try {
      setRelayState(plan.relay, true, PULSE_INITIATOR);

      if (plan.durationMs > 0) {
        scheduler.schedule(() -> {
          try {
            Relay currentRelay = Relay.findByIdentifier(plan.relay.identifier);
            if (currentRelay != null && RelayLog.isRelayOnTooLong(currentRelay, plan.durationMs)) {
              setRelayState(currentRelay, false, PULSE_INITIATOR);
              LOG.info("Pulse ended for {} after {}ms", plan.relay.identifier, plan.durationMs);
            }
          } catch (Exception e) {
            LOG.error("Failed to end pulse for {}: {}", plan.relay.identifier, e.getMessage());
          }
        }, plan.durationMs, TimeUnit.MILLISECONDS);
      }
    } catch (Exception e) {
      LOG.error("Failed to execute pulse for {}: {}", plan.relay.identifier, e.getMessage());
      notifyError(plan, e);
    }
  }

  private void setRelayState(Relay relay, boolean state, String initiator) {
    Map<String, Boolean> relayState = new HashMap<>();
    relayState.put(relay.identifier, state);
    try {
      SatelliteClient client = satelliteService.createSatelliteClient(relay.satellite.ip);
      client.updateRelayState(relayState);
    } catch (Exception e) {
      LOG.warn("ESP32 communication failed for {}: {}", relay.identifier, e.getMessage());
    }
    persistRelaySwitch(relay, initiator, state);
  }

  @Transactional
  void persistRelaySwitch(Relay relay, String initiator, boolean state) {
    Relay freshRelay = Relay.findByIdentifier(relay.identifier);
    if (freshRelay == null) return;
    freshRelay.value = state;
    new RelayLog(freshRelay, initiator, new Date(), state).persist();
  }

  private long getPulseDuration(RelayAction action, Relay relay) {
    String id = relay.identifier;
    if (!action.desiredState) return 0;
    if (id.startsWith("relay_line") && !id.equals("relay_line7") && !id.equals("relay_line8")) {
      return IRRIGATION_PULSE_MS;
    }
    return 0;
  }

  private void notifyError(PulsePlan plan, Exception error) {
    try {
      telegramClient.sendMessage(botToken, chatId,
          "⚠️ AI-Pulse fehlgeschlagen: " + plan.relay.identifier + " – " + error.getMessage());
    } catch (Exception e) {
      LOG.warn("Telegram notification failed", e);
    }
  }

  public record PulsePlan(RelayAction action, Relay relay, long durationMs, boolean shadow) {
    public String durationLabel() {
      if (durationMs <= 0) return "dauerhaft";
      if (durationMs < 60_000) return durationMs / 1000 + "s";
      return durationMs / 60_000 + "min";
    }
  }
}
