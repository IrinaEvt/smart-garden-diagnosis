package com.example.smart_garden.controllers;

import com.example.smart_garden.entities.PlantFamilyEntity;
import com.example.smart_garden.repositories.PlantFamilyRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/families")
public class PlantFamilyController {

    private final PlantFamilyRepository plantFamilyRepository;

    public PlantFamilyController(PlantFamilyRepository repo) {
        this.plantFamilyRepository = repo;
    }

    @GetMapping
    public List<PlantFamilyEntity> getAllFamilies() {
        return plantFamilyRepository.findAll();
    }
}
