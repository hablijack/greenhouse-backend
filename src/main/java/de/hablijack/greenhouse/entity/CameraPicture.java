package de.hablijack.greenhouse.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "camera_picture", schema = "greenhouse")
public class CameraPicture extends PanacheEntity {

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  public Date timestamp;
  @Lob
  @Column(name = "image_byte", nullable = false)
  public byte[] imageByte;

  public CameraPicture() {
  }

  public CameraPicture(Date timestamp, byte[] imageByte) {
    this.timestamp = timestamp;
    this.imageByte = imageByte;
  }

  public static CameraPicture findExistingOrCreteNew() {
    if (findAll().count() == 0) {
      return new CameraPicture();
    } else {
      return (CameraPicture) findAll().list().get(0);
    }
  }
}
