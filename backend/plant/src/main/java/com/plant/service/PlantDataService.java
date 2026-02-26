package com.plant.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.plant.dao.PlantData;
import com.plant.repository.PlantDataRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PlantDataService {

    // Database access
    private final PlantDataRepository repository;

    public PlantDataService(PlantDataRepository repository) {
        this.repository = repository;
    }

    // Find all plant data entries
    public List<PlantData> findAll() {
        log.info("Fetching all records");
        return repository.findAll();
    }

    // Find a data entry by ID
    public Optional<PlantData> findById(Long id) {
        log.info("Fetching record " + id);
        return repository.findById(id);
    }

    // Save plant data to the DB
    public PlantData save(PlantData plantData) {
        log.info("Saving record " + plantData);
        return repository.save(plantData);
    }

    // Update existing plant data, or create new one with a specified ID
    public PlantData update(PlantData newPlant, Long id) {
        log.info("Updating record " + newPlant + " " + id);
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
        log.info("Deleting record " + id);
        repository.deleteById(id);
    }

    // Get all readings from the last 7 days
    public List<PlantData> getLastSevenDays() {
        log.info("Fetching the readings from the last 7 days...");
        return repository.findByDateAfter(LocalDateTime.now().minusDays(7));
    }

    // Get all the readings from the last 30 days
    public List<PlantData> getLastThirtyDays() {
        log.info("Fetching the readings from the last 30 days...");
        return repository.findByDateAfter(LocalDateTime.now().minusDays(30));
    }

    public void deleteOlderThanSixtyDays() {
        log.info("Deleting records older than 60 days...");
        repository.deleteByDateBefore(LocalDateTime.now().minusDays(60));
    }
}