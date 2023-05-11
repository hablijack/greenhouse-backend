package de.hablijack.greenhouse.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "satellite", schema = "greenhouse")
public class Satellite extends PanacheEntity {

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
  @JsonBackReference
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "satellite")
  public Set<Relay> relays;

  public Satellite(String identifier, String name, String imageUrl, String description, String ip, boolean online) {
    this.identifier = identifier;
    this.name = name;
    this.imageUrl = imageUrl;
    this.description = description;
    this.ip = ip;
    this.online = online;
  }

  public Satellite() {
  }

  public static Satellite findByIdentifier(String id) {
    return (Satellite) find("identifier = ?1", id).list().get(0);
  }

  public static Satellite findByIp(String ip) {
    return (Satellite) find("ip = ?1", ip).list().get(0);
  }


  public Satellite persistIfNotExist() {
    if (find("identifier = ?1", identifier).count() == 0) {
      this.persist();
      return this;
    } else {
      return (Satellite) find("identifier = ?1", identifier).list().get(0);
    }
  }
}
