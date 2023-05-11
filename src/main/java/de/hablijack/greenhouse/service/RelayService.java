package de.hablijack.greenhouse.service;

import de.hablijack.greenhouse.entity.Relay;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RelayService {

  @Transactional
  public String getSatelliteBaseUrlForRelay(Relay relay) {
    return "http://" + relay.satellite.ip;
  }
}
