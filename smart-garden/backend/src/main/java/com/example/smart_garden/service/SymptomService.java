package com.example.smart_garden.service;

import com.example.smart_garden.entities.PlantEntity;
import com.example.smart_garden.entities.SymptomEntity;
import com.example.smart_garden.repositories.PlantRepository;
import com.example.smart_garden.repositories.SymptomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SymptomService {
    private final SymptomRepository symptomRepo;

    public SymptomService(SymptomRepository symptomRepo) {
        this.symptomRepo = symptomRepo;
    }

    public List<SymptomEntity> getSymptomsByPlant(Long plantId) {
        return symptomRepo.findByPlantId(plantId);
    }

    public boolean deleteSymptomById(Long id) {
        Optional<SymptomEntity> symptomOpt = symptomRepo.findById(id);
        if (symptomOpt.isPresent()) {
            symptomRepo.deleteById(id);
            return true;
        }
        return false;
    }
}

