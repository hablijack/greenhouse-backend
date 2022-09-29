package de.hablijack.greenhouse.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "satelite", schema = "greenhouse")
public class Satelite extends PanacheEntity {

  @Column(name = "identifier", nullable = false, unique = true)
  public String identifier;
  @Column(name = "name", nullable = false)
  public String name;
  @Column(name = "image_url", nullable = false)
  public String imageUrl;
  @Column(name = "description", nullable = false)
  public String description;
  @Column(name = "ip", nullable = false)
  public String ip;
  @Column(name = "online", nullable = false)
  public boolean online;

  public Satelite(String identifier, String name, String imageUrl, String description, String ip, boolean online) {
    this.identifier = identifier;
    this.name = name;
    this.imageUrl = imageUrl;
    this.description = description;
    this.ip = ip;
    this.online = online;
  }

  public Satelite() {
  }

  public static Satelite findByIdentifier(String id) {
    return (Satelite) find("identifier = ?1", id).list().get(0);
  }

  public static Satelite findByIp(String ip) {
    return (Satelite) find("ip = ?1", ip).list().get(0);
  }


  public Satelite persistIfNotExist() {
    if (find("identifier = ?1", identifier).count() == 0) {
      this.persist();
      return this;
    } else {
      return (Satelite) find("identifier = ?1", identifier).list().get(0);
    }
  }
}
