package com.example.smart_garden.service;

import com.example.smart_garden.entities.PlantInfoSymptomEntity;
import com.example.smart_garden.repositories.PlantSymptomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlantInfoSymptomService {

    private final PlantSymptomRepository repository;

    public PlantInfoSymptomService(PlantSymptomRepository repository) {
        this.repository = repository;
    }

    public List<PlantInfoSymptomEntity> getAll() {
        return repository.findAll();
    }

    public PlantInfoSymptomEntity getById(Long id) {
        return repository.findById(id).orElse(null);
    }
}

