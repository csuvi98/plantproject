package com.plant.exception;

public class PlantDataNotFoundException extends RuntimeException{
    
    public PlantDataNotFoundException(Long id) {
        super("Requested plant data is missing! id: " + id);
    }
    
}
