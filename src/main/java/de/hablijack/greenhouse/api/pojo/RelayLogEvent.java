package de.hablijack.greenhouse.api.pojo;

public class RelayLogEvent {

  private String initiator;
  private boolean newValue;

  public RelayLogEvent(String initiator, boolean newValue) {
    this.initiator = initiator;
    this.newValue = newValue;
  }

  public RelayLogEvent() {
  }

  public boolean getNewValue() {
    return newValue;
  }

  public void setNewValue(boolean newValue) {
    this.newValue = newValue;
  }

  public String getInitiator() {
    return initiator;
  }

  public void setInitiator(String initiator) {
    this.initiator = initiator;
  }

}
