package com.plant.dao;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class PlantData {

    @Id
    @GeneratedValue
    private Long id;

    // Date of measurement
    private LocalDateTime date;
    // Raw sensor reading value
    private int reading;
    // Sensor value in percentage
    private int percentage;

    public PlantData() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public int getReading() {
        return reading;
    }

    public void setReading(int reading) {
        this.reading = reading;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    // Before writing the entry to the DB, calculate the percentage value
    // This way it's always calculated
    @jakarta.persistence.PrePersist
    @jakarta.persistence.PreUpdate
    public void calculatePercentage() {
        this.percentage = (int) (((double) this.reading / 4096.0) * 100.0);
    }

}
