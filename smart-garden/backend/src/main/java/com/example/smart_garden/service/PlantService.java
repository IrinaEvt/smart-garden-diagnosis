package com.example.smart_garden.service;


import com.example.smart_garden.entities.PlantEntity;
import com.example.smart_garden.entities.SymptomEntity;
import com.example.smart_garden.entities.UserEntity;
import com.example.smart_garden.ontology.PlantOntology;
import com.example.smart_garden.repositories.PlantRepository;
import com.example.smart_garden.repositories.SymptomRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public List<String> getReasoning(String plantName) {
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

    public Optional<PlantEntity> getPlantByName(String name) {
        return plantRepo.findById(name);
    }
}
