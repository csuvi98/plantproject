package com.plant.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.plant.dao.PlantData;
import com.plant.repository.PlantDataRepository;

@Service
public class PlantDataService {

    private final PlantDataRepository repository;

    final int MAX_READING_VALUE = 4096;

    public PlantDataService(PlantDataRepository repository) {
        this.repository = repository;
    }

    public List<PlantData> findAll() {
        return repository.findAll();
    }

    public Optional<PlantData> findById(Long id) {
        return repository.findById(id);
    }

    public PlantData save(PlantData plantData) {
        return repository.save(plantData);
    }

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

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public List<PlantData> getLastSevenDays() {
        return repository.findByDateAfter(LocalDateTime.now().minusDays(7));
    }

    public List<PlantData> getLastThirtyDays() {
        return repository.findByDateAfter(LocalDateTime.now().minusDays(30));
    }

    public int readingToPercentage(int reading) {
        double readingInDouble = (double) reading;
        double maxValueInDouble = (double) MAX_READING_VALUE;
        double percentage = (readingInDouble / maxValueInDouble) * 100.0;
        int result = (int) percentage;

        return result;
    }
}