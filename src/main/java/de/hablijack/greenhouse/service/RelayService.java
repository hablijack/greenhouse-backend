package de.hablijack.greenhouse.service;

import de.hablijack.greenhouse.entity.Relay;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class RelayService {

  @Transactional
  public List<Relay> getAllRelays() {
    return Relay.listAll();
  }

  @Transactional
  public String getSatelliteBaseUrlForRelay(Relay relay) {
    return "http://" + relay.satellite.ip;
  }
}
