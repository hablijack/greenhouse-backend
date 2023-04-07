package de.hablijack.greenhouse.api.pojo;

public class TelegramTextMessage {

  private String text;

  public TelegramTextMessage() {
  }

  public TelegramTextMessage(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}
