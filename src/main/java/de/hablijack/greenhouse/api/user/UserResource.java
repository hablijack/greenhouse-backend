package de.hablijack.greenhouse.api.user;

import de.hablijack.greenhouse.api.pojo.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/api/rest")
public class UserResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/users")
  public List<User> getAllUsers() {
    List<User> existingUsers = new ArrayList<>();
    for (PanacheEntityBase dbUser : de.hablijack.greenhouse.entity.User.findAll().list()) {
      User user = new User();
      de.hablijack.greenhouse.entity.User currentUser = (de.hablijack.greenhouse.entity.User) dbUser;
      user.setUsername(currentUser.username);
      user.setRole(currentUser.role);
      user.setName(currentUser.name);
      existingUsers.add(user);
    }
    return existingUsers;
  }

  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/users/{username}")
  @Transactional
  public de.hablijack.greenhouse.entity.User updateUser(@PathParam("username") String username, User newUser) {
    de.hablijack.greenhouse.entity.User oldUser = de.hablijack.greenhouse.entity.User.findByUsername(username);
    oldUser.username = newUser.getUsername();
    oldUser.name = newUser.getName();
    oldUser.role = newUser.getRole();
    oldUser.password = BcryptUtil.bcryptHash(newUser.getPassword());
    oldUser.persist();
    return oldUser;
  }
}
