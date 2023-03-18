package de.hablijack.greenhouse.api.pojo;

import java.util.Date;

public class ChartjsData {
  private Date x;
  private Double y;

  public ChartjsData(Date x, Double y) {
    this.x = x;
    this.y = y;
  }

  public Date getX() {
    return x;
  }
  public void setX(Date x) {
    this.x = x;
  }
  public Double getY() {
    return y;
  }
  public void setY(Double y) {
    this.y = y;
  }
}
