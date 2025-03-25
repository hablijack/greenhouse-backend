package de.hablijack.greenhouse.entity;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "greenhouse_user")
public class User extends PanacheEntity {

  @Username
  public String username;
  @Column(name = "name", nullable = false)
  public String name;
  @Password
  public String password;
  @Roles
  public String role;

  public User() {
  }

  public User(String username, String name, String password, String role) {
    this.username = username;
    this.name = name;
    this.password = BcryptUtil.bcryptHash(password);
    this.role = role;
  }

  public static User findByUsername(String username) {
    return find("username = ?1", username).firstResult();
  }

  public User persistIfNotExist() {
    if (find("username = ?1", this.username).count() == 0) {
      this.persist();
      return this;
    } else {
      return find("username = ?1", this.username).firstResult();
    }
  }

  public boolean isValidPassword(String inputPassword) {
    return BcryptUtil.matches(inputPassword, this.password);
  }
}
