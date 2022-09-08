package eu.hablijack.api.sensor;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api")
public class MeasurementResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/sensor/measurement/live")
  public String hello() {


    /*return {
        "air_temp_inside": self.__get_current_air_temp_inside_value(),
        "light_inside": self.__get_current_light_inside_value(),
        "humidity_inside": self.__get_current_humidity_inside_value(),
        "soil_temp_inside": self.__get_current_soil_temp_inside_value(),
        "air_temp_outside": self.__get_current_air_temp_outside_value(),
        "co2_inside": self.__get_current_co2_inside_value(),
        "battery_capacity": self.__get_current_battery_capacity_value(),
        "wifi_strength": self.__get_current_wifi_strength_value()
        }*/


    return "Hello from RESTEasy Reactive";
  }
}