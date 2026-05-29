package de.hablijack.greenhouse.ai.service;

import de.hablijack.greenhouse.ai.api.dto.PlantRequest;
import de.hablijack.greenhouse.ai.api.dto.PlantResponse;
import de.hablijack.greenhouse.ai.entity.Plant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PlantService {

  @SuppressWarnings("unchecked")
  public List<PlantResponse> getAllPlants() {
    List<Plant> plants = (List<Plant>) (List<?>) Plant.listAll();
    List<PlantResponse> responses = new ArrayList<>();
    for (Plant plant : plants) {
      responses.add(toResponse(plant));
    }
    return responses;
  }

  public PlantResponse getPlant(Long id) {
    Plant plant = Plant.findById(id);
    if (plant == null) {
      throw new NotFoundException("Plant not found: " + id);
    }
    return toResponse(plant);
  }

  @Transactional
  public PlantResponse createPlant(PlantRequest request) {
    Plant existing = Plant.findByName(request.name);
    if (existing != null) {
      return toResponse(existing);
    }
    Plant plant = new Plant(request.name, request.description);
    plant.persist();
    return toResponse(plant);
  }

  @Transactional
  public void deletePlant(Long id) {
    Plant plant = Plant.findById(id);
    if (plant == null) {
      throw new NotFoundException("Plant not found: " + id);
    }
    plant.delete();
  }

  private PlantResponse toResponse(Plant plant) {
    return new PlantResponse(plant.id, plant.name, plant.description);
  }
}
