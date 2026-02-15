package com.plant.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.plant.dao.PlantData;
import com.plant.repository.PlantDataRepository;

@Service
public class PlantDataService {

    // Database access
    private final PlantDataRepository repository;

    /*
     * The sensor reads values from 0 to 4096,
     * with 4096 being the moisture value of dry air,
     * and 0 being 100% wet.
     */
    final int MAX_READING_VALUE = 4096;

    public PlantDataService(PlantDataRepository repository) {
        this.repository = repository;
    }

    // Find all plant data entries
    public List<PlantData> findAll() {
        return repository.findAll();
    }

    // Find a data entry by ID
    public Optional<PlantData> findById(Long id) {
        return repository.findById(id);
    }

    // Save plant data to the DB
    public PlantData save(PlantData plantData) {
        return repository.save(plantData);
    }

    // Update existing plant data, or create new one with a specified ID
    public PlantData update(PlantData newPlant, Long id) {
        return repository.findById(id)
                .map(plantData -> {
                    plantData.setDate(newPlant.getDate());
                    plantData.setReading(newPlant.getReading());
                    return repository.save(plantData);
                })
                .orElseGet(() -> {
                    newPlant.setId(id);
                    return repository.save(newPlant);
                });
    }

    // Delete entry by ID
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    // Get all readings from the last 7 days
    public List<PlantData> getLastSevenDays() {
        return repository.findByDateAfter(LocalDateTime.now().minusDays(7));
    }

    // Get all the readings from the last 30 days
    public List<PlantData> getLastThirtyDays() {
        return repository.findByDateAfter(LocalDateTime.now().minusDays(30));
    }
}