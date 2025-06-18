package com.example.smart_garden.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SensorReadingEntity {
    @Id
    @GeneratedValue
    private Long id;

    private String parameter; // "temperature", "light", "humidity", "soilMoisture"
    private String value;     // "low", "medium", "high"
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

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public PlantEntity getPlant() { return plant; }
    public void setPlant(PlantEntity plant) { this.plant = plant; }
}
