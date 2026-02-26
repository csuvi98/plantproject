package com.plant.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private final PlantDataService plantDataService;

    public ScheduledTasks(PlantDataService plantDataService) {
        this.plantDataService = plantDataService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldData() {
        plantDataService.deleteOlderThanSixtyDays();
    }

}
