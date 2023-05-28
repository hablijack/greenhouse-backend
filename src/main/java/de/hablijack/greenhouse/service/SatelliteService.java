package de.hablijack.greenhouse.service;

import de.hablijack.greenhouse.webclient.SatelliteClient;
import jakarta.enterprise.context.ApplicationScoped;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

@ApplicationScoped
public class SatelliteService {

  private static final Long CONNECT_TIMEOUT = Long.valueOf(10000);
  private static final Long READ_TIMEOUT = Long.valueOf(10000);

  public static BufferedImage rotate(BufferedImage bimg, Double angle) {
    double sin = Math.abs(Math.sin(Math.toRadians(angle)));
    double cos = Math.abs(Math.cos(Math.toRadians(angle)));
    int w = bimg.getWidth();
    int h = bimg.getHeight();
    int neww = (int) Math.floor(w * cos + h * sin);
    int newh = (int) Math.floor(h * cos + w * sin);
    BufferedImage rotated = new BufferedImage(neww, newh, bimg.getType());
    Graphics2D graphic = rotated.createGraphics();
    graphic.translate((neww - w) / 2, (newh - h) / 2);
    graphic.rotate(Math.toRadians(angle), w / 2, h / 2);
    graphic.drawRenderedImage(bimg, null);
    graphic.dispose();
    return rotated;
  }

  public SatelliteClient createSatelliteClient(String ip) throws MalformedURLException {
    return RestClientBuilder.newBuilder()
        .baseUrl(new URL("http://" + ip))
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
        .build(SatelliteClient.class);
  }
}
