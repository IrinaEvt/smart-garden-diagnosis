package com.example.smart_garden.models;


import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Plant implements Serializable {

    private String type;
    private String name;
    private String soilMoisture;      // e.g. 10–90
    private String temperature;       // e.g. 5–40
    private String humidity;          // e.g. 10–90
    private String light;             // e.g. 100–1000 (lux)
    private List<String> symptoms;
    private String family;

    public Plant() {}

    public Plant(String type, String name, String soilMoisture, String temperature, String humidity, String light,List<String> symptoms, String family) {
        this.name = name;
        this.type = type;
        this.soilMoisture = soilMoisture;
        this.temperature = temperature;
        this.humidity = humidity;
        this.light = light;
        this.family = family;
        this.symptoms = symptoms;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSoilMoisture() {
        return soilMoisture;
    }

    public void setSoilMoisture(String soilMoisture) {
        this.soilMoisture = soilMoisture;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getLight() {
        return light;
    }

    public void setLight(String light) {
        this.light = light;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Plant [name=").append(name).append(", type=").append(type).append("]\n");
        sb.append("----- Symptoms -----\n");
        for (String symptom : symptoms) {
            sb.append("- ").append(symptom).append("\n");
        }

        sb.append("---- Needs ----\n");
        sb.append("Humidity: ").append(humidity).append("\n");
        sb.append("Temperature: ").append(temperature).append("\n");
        sb.append("Light: ").append(light).append("\n");
        sb.append("Water: ").append(soilMoisture).append("\n");
        sb.append("Family: ").append(family).append("\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plant plant = (Plant) o;
        return Objects.equals(name, plant.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}

