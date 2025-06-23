package com.example.smart_garden.service;


import com.example.smart_garden.entities.PlantEntity;
import com.example.smart_garden.entities.SymptomEntity;
import com.example.smart_garden.entities.UserEntity;
import com.example.smart_garden.ontology.PlantOntology;
import com.example.smart_garden.repositories.PlantRepository;
import com.example.smart_garden.repositories.SymptomRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlantService {
    private final PlantRepository plantRepo;
    private final SymptomRepository symptomRepo;
    private final PlantOntology ontology;

    public PlantService(PlantRepository plantRepo, SymptomRepository symptomRepo) {
        this.plantRepo = plantRepo;
        this.symptomRepo = symptomRepo;
        this.ontology = new PlantOntology();
    }

    private NeedRange interpretRange(String level, String parameter) {
        return switch (parameter) {
            case "temperature" -> "high".equalsIgnoreCase(level) ? new NeedRange(22, 28) : new NeedRange(16, 22);
            case "light"       -> "high".equalsIgnoreCase(level) ? new NeedRange(600, 1000) : new NeedRange(200, 600);
            case "humidity"    -> "high".equalsIgnoreCase(level) ? new NeedRange(60, 80) : new NeedRange(30, 60);
            case "soilMoisture"-> "high".equalsIgnoreCase(level) ? new NeedRange(50, 80) : new NeedRange(20, 50);
            default -> new NeedRange(0, 0); // fallback
        };
    }



    public PlantEntity savePlant(PlantEntity plant, UserEntity user) {
        ontology.createPlantIndividual(plant.toPlantModel());

        Map<String, String> needs = getNeedsFromPlantType(plant.getType());
        plant.setTemperature(needs.get("temperature"));
        plant.setHumidity(needs.get("humidity"));
        plant.setLight(needs.get("light"));
        plant.setSoilMoisture(needs.get("soilMoisture"));

        plant.setUser(user);
        return plantRepo.save(plant);
    }

    public List<PlantEntity> getPlantsForUser(UserEntity user) {
        return plantRepo.findByUser(user);
    }


    public void addSymptom(String plantName, String symptomName) {
        Optional<PlantEntity> optional = plantRepo.findById(plantName);
        if (optional.isPresent()) {
            PlantEntity plant = optional.get();
            SymptomEntity symptom = new SymptomEntity();
            symptom.setName(symptomName);
            symptom.setPlant(plant);
            symptomRepo.save(symptom);

            ontology.createSymptomForPlant(plantName, symptomName);
        }
    }

    public Map<String, List<String>> getSymptomOptionsGrouped() {
        return ontology.getAllSymptomsGroupedByCategory();
    }

    public List<Map<String, String>> getAllPlantTypes() {
        return ontology.getAllPlantTypesWithFamilies();
    }

    public List<String> suggestPlantsFromSameFamily(String plantTypeName) {
        return ontology.suggestPlantsFromSameFamily(plantTypeName);
    }

    public Optional<String> suggestEasyCarePlant() {
        return ontology.suggestEasyCarePlant();
    }

    public List<ReasoningBlock> getReasoning(String plantName) {
        return ontology.getAdviceForPlantIndividual(plantName);
    }

    public Map<String, String> getNeedsFromPlantType(String typeName) {
        return ontology.getNeedsFromPlantType(typeName);
    }


    public List<PlantEntity> getAllPlants() {
        return plantRepo.findAll();
    }

    public boolean deletePlant(String plantName, UserEntity user) {
        Optional<PlantEntity> optional = plantRepo.findById(plantName);
        if (optional.isPresent()) {
            PlantEntity plant = optional.get();
            if (plant.getUser() != null && plant.getUser().getId().equals(user.getId())) {
                plantRepo.delete(plant);
                return true;
            }
        }
        return false;
    }
    public PlantEntity findByIdOrThrow(String name) {
        return plantRepo.findById(name)
                .orElseThrow(() -> new RuntimeException("Plant not found: " + name));
    }

    public Optional<PlantEntity> getPlantByName(String name) {
        return plantRepo.findById(name);
    }

    public List<String> evaluatePlantHealth(PlantEntity plant, Map<String, Double> sensorData) {
        List<String> alerts = new ArrayList<>();

        if (sensorData == null || sensorData.isEmpty()) {
            alerts.add("❗ Липсват данни от сензорите.");
            return alerts;
        }

        checkParameter(alerts, "temperature", plant.getTemperature(), sensorData.get("temperature"));
        checkParameter(alerts, "light", plant.getLight(), sensorData.get("light"));
        checkParameter(alerts, "humidity", plant.getHumidity(), sensorData.get("humidity"));
        checkParameter(alerts, "soilMoisture", plant.getSoilMoisture(), sensorData.get("soilMoisture"));

        return alerts;
    }

    private void checkParameter(List<String> alerts, String paramName, String expectedLevel, Double actualValue) {
        NeedRange range = interpretRange(expectedLevel, paramName);
        if (!range.isInRange(actualValue)) {
            alerts.add("❗ Несъответствие в " + paramName +
                    ": нужно '" + expectedLevel +
                    "', стойност: " + actualValue);
        }
    }




}
