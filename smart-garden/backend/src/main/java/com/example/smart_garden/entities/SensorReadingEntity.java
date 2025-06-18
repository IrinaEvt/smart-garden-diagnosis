package com.example.smart_garden.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SensorReadingEntity {
    @Id
    @GeneratedValue
    private Long id;

    private String parameter;


    private double readingValue;


    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "plant_name")
    private PlantEntity plant;

    public SensorReadingEntity() {
        this.timestamp = LocalDateTime.now();
    }

    // getters и setters
    public Long getId() { return id; }
    public String getParameter() { return parameter; }
    public void setParameter(String parameter) { this.parameter = parameter; }

    public double getReadingValue() {
        return readingValue;
    }

    public void setReadingValue(double readingValue) {
        this.readingValue = readingValue;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public PlantEntity getPlant() { return plant; }
    public void setPlant(PlantEntity plant) { this.plant = plant; }
}
