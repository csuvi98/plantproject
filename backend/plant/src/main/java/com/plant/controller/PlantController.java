package com.plant.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.plant.dao.PlantData;
import com.plant.exception.PlantDataNotFoundException;
import com.plant.service.PlantDataService;

@RestController
public class PlantController {

    private final PlantDataService plantDataService;

    // Standard constructor injection (Preferred over @Autowired on fields)
    PlantController(PlantDataService plantDataService) {
        this.plantDataService = plantDataService;
    }

    @GetMapping("/plantdata")
    public List<PlantData> getAll() {
        return plantDataService.findAll();
    }

    @GetMapping("/plantdata/{id}")
    public PlantData getSingleData(@PathVariable Long id) {
        return plantDataService.findById(id)
                .orElseThrow(() -> new PlantDataNotFoundException(id));
    }

    @GetMapping("/plantdata/week")
    public List<PlantData> getLastSeven() {
        return plantDataService.getLastSevenDays();
    }

    @GetMapping("/plantdata/month")
    public List<PlantData> getLastThirty() {
        return plantDataService.getLastThirtyDays();
    }

    @PostMapping("/plantdata")
    public PlantData newData(@RequestBody PlantData newPlantData) {
        return plantDataService.save(newPlantData);
    }

    @PutMapping("/plantdata/{id}")
    public PlantData replacePlantData(@RequestBody PlantData newPlant, @PathVariable Long id) {
        return plantDataService.update(newPlant, id);
    }

    @DeleteMapping("/plantdata/{id}")
    public void deletePlantData(@PathVariable Long id) {
        plantDataService.deleteById(id);
    }
}