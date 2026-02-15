package com.plant.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.plant.dao.PlantData;
import com.plant.exception.PlantDataNotFoundException;
import com.plant.service.PlantDataService;

/*  The class is not HATEOAS compliant yet, as the dashboard
    that visualizes the data is super simple. */
@RestController
public class PlantController {

    // Contains methods for interacting with the DB and other utilities
    private final PlantDataService plantDataService;

    PlantController(PlantDataService plantDataService) {
        this.plantDataService = plantDataService;
    }

    // Get all the plant data entries
    @GetMapping("/plantdata")
    public List<PlantData> getAll() {
        return plantDataService.findAll();
    }

    // Get a specific entry
    @GetMapping("/plantdata/{id}")
    public PlantData getSingleData(@PathVariable Long id) {
        return plantDataService.findById(id)
                .orElseThrow(() -> new PlantDataNotFoundException(id));
    }

    // Get the data gathered from the last 7 days
    @GetMapping("/plantdata/week")
    public List<PlantData> getLastSeven() {
        return plantDataService.getLastSevenDays();
    }

    // Get the data gathered from the last 30 days
    @GetMapping("/plantdata/month")
    public List<PlantData> getLastThirty() {
        return plantDataService.getLastThirtyDays();
    }

    // Post new data
    @PostMapping("/plantdata")
    public PlantData newData(@RequestBody PlantData newPlantData) {
        return plantDataService.save(newPlantData);
    }

    // Update existing data
    @PutMapping("/plantdata/{id}")
    public PlantData replacePlantData(@RequestBody PlantData newPlant, @PathVariable Long id) {
        return plantDataService.update(newPlant, id);
    }

    // Delete an entry
    @DeleteMapping("/plantdata/{id}")
    public void deletePlantData(@PathVariable Long id) {
        plantDataService.deleteById(id);
    }
}