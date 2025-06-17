package com.example.smart_garden.controllers;

import com.example.smart_garden.entities.PlantEntity;
import com.example.smart_garden.service.PlantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/plants")
@CrossOrigin(origins = "*")
public class PlantController {

    private final PlantService plantService;

    public PlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    @GetMapping
    public List<PlantEntity> getAllPlants() {
        return plantService.getAllPlants();
    }

    @PostMapping
    public PlantEntity createPlant(@RequestBody PlantEntity plant) {
        return plantService.savePlant(plant);
    }

    @DeleteMapping("/{plantName}")
    public ResponseEntity<Void> deletePlant(@PathVariable String plantName) {
        plantService.deletePlant(plantName);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{typeName}/needs")
    public Map<String, String> getTypeNeeds(@PathVariable String typeName) {
        return plantService.getNeedsFromPlantType(typeName);
    }
}
