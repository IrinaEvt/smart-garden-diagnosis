package com.example.smart_garden.controllers;

import com.example.smart_garden.entities.PlantInfoSymptomEntity;
import com.example.smart_garden.service.PlantInfoSymptomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/symptoms")
@CrossOrigin(origins = "*")
public class PlantSymptomController {

    private final PlantInfoSymptomService symptomService;

    public PlantSymptomController(PlantInfoSymptomService symptomService) {
        this.symptomService = symptomService;
    }

    @GetMapping
    public List<PlantInfoSymptomEntity> getAllSymptoms() {
        return symptomService.getAll();
    }

    @GetMapping("/{id}")
    public PlantInfoSymptomEntity getSymptomById(@PathVariable Long id) {
        return symptomService.getById(id);
    }
}
