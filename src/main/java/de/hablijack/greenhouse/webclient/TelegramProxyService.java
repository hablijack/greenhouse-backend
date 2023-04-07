package de.hablijack.greenhouse.webclient;


import de.hablijack.greenhouse.api.pojo.TelegramTextMessage;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/send_text_message")
@RegisterRestClient
public interface TelegramProxyService {

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  Response sendTextMessage(TelegramTextMessage message);
}
