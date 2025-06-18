package com.example.smart_garden.controllers;

import com.example.smart_garden.entities.PlantEntity;
import com.example.smart_garden.entities.SensorReadingEntity;
import com.example.smart_garden.entities.UserEntity;
import com.example.smart_garden.repositories.UserRepository;
import com.example.smart_garden.service.NeedRange;
import com.example.smart_garden.service.PlantService;
import com.example.smart_garden.service.SensorDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/plants")
@CrossOrigin(origins = "*")
public class PlantController {

    private final PlantService plantService;
    private final SensorDataService sensorDataService;
    private final UserRepository userRepo;

    public PlantController(PlantService plantService, UserRepository userRepo, SensorDataService sensorDataService) {
        this.plantService = plantService;
        this.userRepo = userRepo;
        this.sensorDataService = sensorDataService;
    }




    // ➕ Създава ново растение за логнатия потребител
    @PostMapping
    public PlantEntity createPlant(@AuthenticationPrincipal UserDetails userDetails,
                                   @RequestBody PlantEntity plant) {
        // Извличаме потребителя от базата по username
        UserEntity user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return plantService.savePlant(plant, user);
    }


    @DeleteMapping("/{plantName}")
    public ResponseEntity<?> deletePlant(@AuthenticationPrincipal UserDetails userDetails,
                                         @PathVariable String plantName) {
        UserEntity user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean deleted = plantService.deletePlant(plantName, user);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(403).body("Нямате достъп");
    }



    // 🔍 Връща нуждите на тип растение (достъпно за всички логнати)
    @GetMapping("/{typeName}/needs")
    public Map<String, String> getTypeNeeds(@AuthenticationPrincipal UserDetails userDetails,@PathVariable String typeName) {
        return plantService.getNeedsFromPlantType(typeName);
    }


    @GetMapping
    public List<PlantEntity> getUserPlants(@AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return plantService.getPlantsForUser(user);
    }

    @GetMapping("/{name}")
    public ResponseEntity<PlantEntity> getPlantByName(
            @PathVariable String name,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserEntity user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return plantService.getPlantByName(name)
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build());
    }

    @GetMapping("/types")
    public List<String> getAllPlantTypes(@AuthenticationPrincipal UserDetails userDetails) {
        return plantService.getAllPlantTypes();
    }

    @GetMapping("/symptoms")
    public Map<String, List<String>> getAllSymptomsGrouped() {
        return plantService.getSymptomOptionsGrouped();
    }


    @GetMapping("/{plantName}/sensors/history")
    public Map<String, Object> getSensorHistory(@PathVariable String plantName,@AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PlantEntity plant = plantService.getPlantByName(plantName)
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Нямате достъп до това растение"));
        sensorDataService.generateRandomReadingsForPlant(plant); // генерира нови
        List<SensorReadingEntity> readings = sensorDataService.getRecentReadingsForPlant(plant);
        List<String> alerts = plantService.evaluatePlantHealth(plant, sensorDataService.extractLastValues(readings));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("readings", readings);
        response.put("alerts", alerts);
        return response;
    }



}
