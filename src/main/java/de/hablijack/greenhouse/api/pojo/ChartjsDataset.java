package de.hablijack.greenhouse.api.pojo;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;

@SuppressFBWarnings({"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class ChartjsDataset {

  private String label;
  private List<ChartjsData> data;
  private String borderColor;
  private int borderWidth;
  private boolean fill;
  private String backgroundColor;

  public ChartjsDataset() {

  }

  public ChartjsDataset(String label, List<ChartjsData> data, String borderColor,
                        int borderWidth, boolean fill, String backgroundColor) {
    this.label = label;
    this.data = data;
    this.borderColor = borderColor;
    this.borderWidth = borderWidth;
    this.fill = fill;
    this.backgroundColor = backgroundColor;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public List<ChartjsData> getData() {
    return data;
  }

  public void setData(List<ChartjsData> data) {
    this.data = data;
  }

  public String getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(String borderColor) {
    this.borderColor = borderColor;
  }

  public int getBorderWidth() {
    return borderWidth;
  }

  public void setBorderWidth(int borderWidth) {
    this.borderWidth = borderWidth;
  }

  public boolean isFill() {
    return fill;
  }

  public void setFill(boolean fill) {
    this.fill = fill;
  }

  public String getBackgroundColor() {
    return backgroundColor;
  }

  public void setBackgroundColor(String backgroundColor) {
    this.backgroundColor = backgroundColor;
  }
}
