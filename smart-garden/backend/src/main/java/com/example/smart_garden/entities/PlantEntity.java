package com.example.smart_garden.entities;


import com.example.smart_garden.models.Plant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class PlantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String type;
    private String temperature;
    private String light;
    private String humidity;
    private String soilMoisture;
    private String imageUrl;
    private String family;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy = "plant", cascade = CascadeType.ALL)
    private List<SymptomEntity> symptoms;

    public Plant toPlantModel() {
        Plant plant = new Plant();
        plant.setName(this.name);
        plant.setType(this.type);
        plant.setTemperature(this.temperature);
        plant.setLight(this.light);
        plant.setHumidity(this.humidity);
        plant.setSoilMoisture(this.soilMoisture);

        if (this.symptoms != null) {
            List<String> symptomNames = this.symptoms.stream()
                    .map(SymptomEntity::getName)
                    .toList();
            plant.setSymptoms(symptomNames);
        }

        return plant;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getLight() {
        return light;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLight(String light) {
        this.light = light;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getSoilMoisture() {
        return soilMoisture;
    }

    public void setSoilMoisture(String soilMoisture) {
        this.soilMoisture = soilMoisture;
    }

    public List<SymptomEntity> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<SymptomEntity> symptoms) {
        this.symptoms = symptoms;
    }

    public List<String> getSymptomsStrings() {
        List<String> symps = new ArrayList<>();

        if (this.symptoms != null) {
            for (int i = 0; i < this.symptoms.size(); i++) {
                SymptomEntity symp = this.symptoms.get(i);
                symps.add(symp.getName());
            }
        }
        return symps;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
