package com.plant.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.plant.dao.PlantData;

public interface PlantDataRepository extends JpaRepository<PlantData, Long> {
    // Find entries from after a date
    List<PlantData> findByDateAfter(LocalDateTime date);

    // Delete all entries older than a given date
    void deleteByDateBefore(LocalDateTime date);
}
