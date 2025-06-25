package com.example.smart_garden.controllers;

import com.example.smart_garden.entities.PlantEntity;
import com.example.smart_garden.entities.SensorReadingEntity;
import com.example.smart_garden.entities.UserEntity;
import com.example.smart_garden.repositories.UserRepository;
import com.example.smart_garden.service.NeedRange;
import com.example.smart_garden.service.PlantService;
import com.example.smart_garden.service.SensorDataService;
import lombok.Lombok;
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

    @PostMapping
    public PlantEntity createPlant(@AuthenticationPrincipal UserDetails userDetails,
                                   @RequestBody PlantEntity plant) {
        // Извличаме потребителя от базата по username
        UserEntity user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return plantService.savePlant(plant, user);
    }


    @DeleteMapping("/{plantId}")
    public ResponseEntity<?> deletePlant(@AuthenticationPrincipal UserDetails userDetails,
                                         @PathVariable Long plantId) {
        UserEntity user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean deleted = plantService.deletePlant(plantId, user);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(403).body("Нямате достъп");
    }

    @GetMapping("/{typeName}/needs")
    public Map<String, String> getTypeNeeds(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String typeName) {
        return plantService.getNeedsFromPlantType(typeName);
    }


    @GetMapping
    public List<PlantEntity> getUserPlants(@AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return plantService.getPlantsForUser(user);
    }

    @GetMapping("/{plantId}")
    public ResponseEntity<PlantEntity> getPlantById(
            @PathVariable Long plantId,
            @AuthenticationPrincipal UserDetails userDetails) {

        UserEntity user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return plantService.getPlantById(plantId)
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(403).build());
    }

    @GetMapping("/suggestions/easy-care")
    public ResponseEntity<String> suggestEasyCarePlant(@RequestParam int issueCount) {
        if (issueCount <= 2) {
            return ResponseEntity.noContent().build(); // без нужда от предложение
        }
        return plantService.suggestEasyCarePlant()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/types")
    public List<Map<String, String>> getAllPlantTypes(@AuthenticationPrincipal UserDetails userDetails) {
        return plantService.getAllPlantTypes();
    }
    @GetMapping("/types/suggestions/{typeName}")
    public List<String> suggestPlants(@PathVariable String typeName) {
        return plantService.suggestPlantsFromSameFamily(typeName);
    }

    @GetMapping("/symptoms")
    public Map<String, List<String>> getAllSymptomsGrouped() {
        return plantService.getSymptomOptionsGrouped();
    }

    @GetMapping("/{plantId}/sensors/history")
    public Map<String, Object> getSensorHistory(@PathVariable Long plantId, @AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PlantEntity plant = plantService.getPlantById(plantId)
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
