package de.hablijack.greenhouse.webclient;

import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/bot{token}")
@RegisterRestClient
public interface TelegramClient {

  @GET
  @Path("/sendMessage")
  @Produces(MediaType.APPLICATION_JSON)
  JsonObject sendMessage(@PathParam("token") String token, @QueryParam("chat_id") String chatId,
                         @QueryParam("text") String text);

  @POST
  @Path("/sendMessage")
  @Produces(MediaType.APPLICATION_JSON)
  JsonObject sendMessageWithBody(@PathParam("token") String token, @QueryParam("chat_id") String chatId,
                                 @QueryParam("text") String text, String body);

  @GET
  @Path("/sendPhoto")
  @Produces(MediaType.APPLICATION_JSON)
  JsonObject sendPhoto(@PathParam("token") String token, @QueryParam("chat_id") String chatId,
                       @QueryParam("photo") String urlOfFoto);
}
