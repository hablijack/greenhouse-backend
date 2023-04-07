package de.hablijack.greenhouse.schedule;

import static io.quarkus.scheduler.Scheduled.ConcurrentExecution.SKIP;

import de.hablijack.greenhouse.api.pojo.TelegramTextMessage;
import de.hablijack.greenhouse.entity.Sensor;
import de.hablijack.greenhouse.service.SensorService;
import de.hablijack.greenhouse.webclient.TelegramProxyService;
import io.quarkus.scheduler.Scheduled;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class DailyReportScheduler {

  @Inject
  @RestClient
  TelegramProxyService telegramProxy;

  @Inject
  SensorService sensorService;

  // @Scheduled(cron = "0 0 8 * * ?", concurrentExecution = SKIP)
  @Scheduled(every = "1m", concurrentExecution = SKIP)
  @Transactional
  public void conditionReport() {
    String messageText = "";
    Map<String, Double> measurements = sensorService.getCurrentSensorValues();
    for (var entry : measurements.entrySet()) {
      String sensorLine = "";
      Sensor sensor = Sensor.findByIdentifier(entry.getKey());
      sensorLine += sensor.name;
      sensorLine += ": " + measurements.get(entry.getKey()) + " " + sensor.unit;
      sensorLine += "\\r\\n";
      messageText += sensorLine;
    }
    if (messageText.length() > 0) {
      TelegramTextMessage message = new TelegramTextMessage();
      message.setText(messageText);
      telegramProxy.sendTextMessage(message);
    }
  }
}
