package de.hablijack.greenhouse.ai.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import de.hablijack.greenhouse.ai.api.dto.RelayDecision;
import de.hablijack.greenhouse.ai.api.dto.RelayDecision.RelayAction;
import de.hablijack.greenhouse.ai.service.AiService;
import de.hablijack.greenhouse.ai.service.RelayPulseService;
import de.hablijack.greenhouse.ai.service.RelayPulseService.PulsePlan;
import de.hablijack.greenhouse.ai.service.SafetyValidator;
import de.hablijack.greenhouse.entity.Relay;
import de.hablijack.greenhouse.webclient.TelegramClient;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AiShadowControlScheduler {

  private static final Logger LOG = LoggerFactory.getLogger(AiShadowControlScheduler.class);

  @Inject
  AiService aiService;

  @Inject
  SafetyValidator safetyValidator;

  @Inject
  RelayPulseService relayPulseService;

  @RestClient
  TelegramClient telegramClient;

  @ConfigProperty(name = "telegram.bot.token")
  String botToken;

  @ConfigProperty(name = "telegram.bot.chatid")
  String chatId;

  @ConfigProperty(name = "ai.shadow.enabled", defaultValue = "false")
  boolean shadowEnabled;

  @Scheduled(every = "300s", concurrentExecution = SKIP)
  void runShadowControl() {
    if (!shadowEnabled) {
      return;
    }

    LOG.info("AI Shadow Control: starting decision cycle");

    try {
      RelayDecision decision = aiService.decideRelayStates();
      if (decision.relays == null || decision.relays.isEmpty()) {
        LOG.info("AI Shadow Control: no relay changes proposed");
        return;
      }

      SafetyValidator.ValidationResult result = safetyValidator.validate(decision.relays);

      List<PulsePlan> pulsePlans = new ArrayList<>();
      for (RelayAction action : result.approved()) {
        Relay relay = Relay.findByIdentifier(action.relayId);
        if (relay != null && action.desiredState) {
          PulsePlan plan = relayPulseService.planPulse(action, relay);
          pulsePlans.add(plan);
        }
      }

      String message = result.formatTelegramMessage(pulsePlans);
      telegramClient.sendMessage(botToken, chatId, message);

      LOG.info("AI Shadow Control: {} approved, {} blocked, {} overrides, {} pulses",
          result.approved().size(), result.blocked().size(), result.overrides().size(),
          pulsePlans.size());

    } catch (Exception e) {
      LOG.warn("AI Shadow Control cycle failed: {}", e.getMessage());
      try {
        telegramClient.sendMessage(botToken, chatId,
            "⚠️ AI Shadow Control fehlgeschlagen: " + e.getMessage());
      } catch (Exception ex) {
        LOG.error("Telegram notification also failed", ex);
      }
    }
  }
}
