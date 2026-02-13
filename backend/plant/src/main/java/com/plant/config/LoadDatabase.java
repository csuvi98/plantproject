package com.plant.config;

import com.plant.dao.PlantData;
import com.plant.repository.PlantDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

@Configuration
class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(PlantDataRepository repository) {
        return args -> {
            log.info("Loading dummy data from CSV...");
            
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    new ClassPathResource("plant_data.csv").getInputStream()))) {
                
                String line;
                boolean isFirstLine = true;
                
                while ((line = br.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue; // Skip the header row
                    }
                    
                    String[] data = line.split(",");
                    
                    // Parse values from CSV
                    LocalDateTime date = LocalDateTime.parse(data[0]);
                    Double reading = Double.parseDouble(data[1]);
                    
                    // Create object and save
                    PlantData plantData = new PlantData();
                    plantData.setDate(date);
                    plantData.setReading(reading);
                    
                    repository.save(plantData);
                }
                log.info("Successfully preloaded plant data entries.");
                
            } catch (Exception e) {
                log.error("Failed to load data from CSV: " + e.getMessage());
            }
        };
    }
}