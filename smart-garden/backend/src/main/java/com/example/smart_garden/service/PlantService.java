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
    private final AgentManagerService agentManager;

    public PlantService(PlantRepository plantRepo, SymptomRepository symptomRepo,AgentManagerService agentManager,PlantOntology ontology) {
        this.plantRepo = plantRepo;
        this.symptomRepo = symptomRepo;
        this.ontology = ontology;
        this.agentManager = agentManager;
    }

    private NeedRange interpretRange(String level, String parameter) {
        return switch (parameter) {
            case "temperature" -> "high".equalsIgnoreCase(level) ? new NeedRange(30, 40) : new NeedRange(2, 20);
            case "light"       -> "high".equalsIgnoreCase(level) ? new NeedRange(700, 1000) : new NeedRange(200, 400);
            case "humidity"    -> "high".equalsIgnoreCase(level) ? new NeedRange(60, 80) : new NeedRange(20, 30);
            case "soilMoisture"-> "high".equalsIgnoreCase(level) ? new NeedRange(70, 100) : new NeedRange(30, 69);
            default -> new NeedRange(0, 0); // fallback
        };
    }

    public List<String> getAllLeafSymptoms() {
        return ontology.getAllLeafSymptoms();
    }

    public PlantEntity savePlant(PlantEntity plant, UserEntity user) {
        ontology.createPlantIndividual(plant.toPlantModel());
        ontology.reloadReasoner();

        Map<String, String> needs = getNeedsFromPlantType(plant.getType());
        plant.setTemperature(needs.get("temperature"));
        plant.setHumidity(needs.get("humidity"));
        plant.setLight(needs.get("light"));
        plant.setSoilMoisture(needs.get("soilMoisture"));

        plant.setUser(user);
        PlantEntity saved = plantRepo.save(plant);
        agentManager.startSensorAgent(saved.getId());
        agentManager.startPlantAgent(saved.getId(),saved.getName());
        System.out.println("📦 Създавам индивид " + plant.getName() + " от тип " + plant.getType());

        return saved;
    }

    public List<PlantEntity> getPlantsForUser(UserEntity user) {
        return plantRepo.findByUser(user);
    }

    public void addSymptom(Long plantId, String symptomName) {
        Optional<PlantEntity> optional = plantRepo.findById(plantId);
        if (optional.isPresent()) {
            PlantEntity plant = optional.get();
            SymptomEntity symptom = new SymptomEntity();
            symptom.setName(symptomName);
            symptom.setPlant(plant);
            symptomRepo.save(symptom);

            ontology.createSymptomForPlant(plant.getType(), symptomName);
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

    public boolean deletePlant(Long plantId, UserEntity user) {
        Optional<PlantEntity> optional = plantRepo.findById(plantId);
        if (optional.isPresent()) {
            PlantEntity plant = optional.get();
            if (plant.getUser() != null && plant.getUser().getId().equals(user.getId())) {
                plantRepo.delete(plant);
                agentManager.stopAgentsForPlant(plantId, plant.getName());
                return true;
            }
        }
        return false;
    }
    public PlantEntity findByIdOrThrow(Long id) {
        return plantRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Plant not found."));
    }

    public Optional<PlantEntity> getPlantById(Long id) {
        return plantRepo.findById(id);
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
