package de.hablijack.greenhouse.service;

import io.quarkus.vertx.ConsumeEvent;
import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.xml.stream.XMLStreamException;

@ApplicationScoped
public class SensorService {
  @Transactional
  @ConsumeEvent(value = "get_current_values", blocking = true)
  public void getCurrentValues() throws IOException, XMLStreamException {

  }
}
