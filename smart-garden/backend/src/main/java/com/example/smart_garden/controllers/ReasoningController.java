package com.example.smart_garden.controllers;

import com.example.smart_garden.entities.PlantEntity;
import com.example.smart_garden.entities.SymptomEntity;
import com.example.smart_garden.models.Plant;
import com.example.smart_garden.repositories.PlantRepository;
import com.example.smart_garden.service.PlantService;
import com.example.smart_garden.service.ReasoningBlock;
import com.example.smart_garden.service.SymptomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reasoning")
@CrossOrigin(origins = "*")
public class ReasoningController {

    private final PlantRepository plantRepo;
    private final PlantService plantService;
    private final SymptomService symptomService;

    public ReasoningController(PlantService plantService, SymptomService symptomService, PlantRepository plantRepo) {
        this.plantRepo = plantRepo;
        this.plantService = plantService;
        this.symptomService = symptomService;
    }

    /*@GetMapping("/{plantName}")
    public ResponseEntity<Map<String, Object>> getReasoning(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String plantName) {
        List<String> rawAdvice = plantService.getReasoning(plantName);
        System.out.println("Raw advice: " + rawAdvice);

        List<String> symptoms = new ArrayList<>();
        List<String> causes = new ArrayList<>();
        List<String> careActions = new ArrayList<>();

        System.out.println("Raw advice: " + rawAdvice);


        for (String line : rawAdvice) {
            if (line.startsWith("Симптом:")) {
                symptoms.add(line.replace("Симптом: ", "").trim());
            } else if (line.startsWith("Възможна причина:")) {
                causes.add(line.replace("Възможна причина: ", "").trim());
            } else if (line.startsWith("Препоръчано действие:")) {
                careActions.add(line.replace("Препоръчано действие: ", "").trim());
            } else if (line.startsWith("Препоръчано действие (клас):")) {
                careActions.add(line.replace("Препоръчано действие (клас): ", "").trim());
            }
        }


        Map<String, Object> response = new LinkedHashMap<>();
        response.put("symptoms", symptoms);
        response.put("causes", causes);
        response.put("careActions", careActions);

        return ResponseEntity.ok(response);
    }*/

    @GetMapping("/{plantId}")
    public ResponseEntity<Map<String, Object>> getReasoning(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId) {


        List<ReasoningBlock> reasoning = Collections.emptyList();
        Optional<PlantEntity> optional = plantRepo.findById(plantId);
        if (optional.isPresent()) {
            PlantEntity plant = optional.get();
            reasoning = plantService.getReasoning(plant.getType());

            reasoning = reasoning.stream()
                .filter(reason -> reason.getSymptoms().stream()
                        .anyMatch(symptom -> plant.getSymptomsStrings().stream().anyMatch(symptom::endsWith)
                        )
                )
                .collect(Collectors.toList());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("reasoning", reasoning);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/{plantId}/symptoms")
    public ResponseEntity<Void> addSymptom(
            @AuthenticationPrincipal UserDetails userDetails,  // 👈 добавено
            @PathVariable Long plantId,
            @RequestBody Map<String, String> payload) {
        String symptom = payload.get("name");
        // Id instead of name?
        plantService.addSymptom(plantId, symptom);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{plantId}/symptoms")
    public List<SymptomEntity> getSymptoms(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId) {
        System.out.println(plantId);
        return symptomService.getSymptomsByPlant(plantId);
    }

    @DeleteMapping("/{plantId}/symptoms/{symptomId}")
    public ResponseEntity<?> deleteSymptom(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId,
            @PathVariable Long symptomId) {
        // PlantId needed?
        symptomService.deleteSymptomById(symptomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/symptom-options")
    public Map<String, List<String>> getSymptomOptions(@AuthenticationPrincipal UserDetails userDetails) {
        return plantService.getSymptomOptionsGrouped();
    }
}
