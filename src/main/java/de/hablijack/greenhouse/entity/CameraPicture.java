package de.hablijack.greenhouse.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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
      return (CameraPicture) listAll().getFirst();
    }
  }
}
