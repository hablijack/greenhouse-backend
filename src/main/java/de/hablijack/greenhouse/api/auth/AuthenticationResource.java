package de.hablijack.greenhouse.api.auth;

import de.hablijack.greenhouse.api.pojo.Auth;
import de.hablijack.greenhouse.entity.User;
import io.quarkus.security.UnauthorizedException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


@Path("/api/rest")
public class AuthenticationResource {

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/authenticate")
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public de.hablijack.greenhouse.api.pojo.User authenticate(Auth authentication) {
    User existingUser = User.findByUsername(authentication.getUsername());
    if (existingUser == null) {
      throw new UnauthorizedException();
    } else {
      if (existingUser.isValidPassword(authentication.getPassword())) {
        de.hablijack.greenhouse.api.pojo.User dtoUser = new de.hablijack.greenhouse.api.pojo.User();
        dtoUser.setName(existingUser.name);
        dtoUser.setUsername(existingUser.username);
        dtoUser.setRole(existingUser.role);
        return dtoUser;
      } else {
        throw new UnauthorizedException();
      }
    }
  }
}
