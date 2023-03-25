package de.hablijack.greenhouse.api.pojo;

import java.util.Date;

public class ChartjsData {
  private Date x;
  private Double y;

  public ChartjsData(Date x, Double y) {
    this.x = (Date) x.clone();
    this.y = y;
  }

  public Date getX() {
    return (Date) x.clone();
  }

  public void setX(Date x) {
    this.x = (Date) x.clone();
  }

  public Double getY() {
    return y;
  }

  public void setY(Double y) {
    this.y = y;
  }
}
