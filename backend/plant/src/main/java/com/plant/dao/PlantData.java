package com.plant.dao;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(indexes = @Index(columnList = "date"))
public class PlantData {

    /*
     * The sensor reads values from 0 to 4096,
     * with 4096 being the moisture value of dry air,
     * and 0 being 100% wet.
     */
    final int MAX_READING_VALUE = 4096;

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
        this.percentage = (int) (((double) (MAX_READING_VALUE - this.reading) / MAX_READING_VALUE) * 100.0);
    }

}
