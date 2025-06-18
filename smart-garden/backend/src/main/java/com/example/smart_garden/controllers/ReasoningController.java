package com.example.smart_garden.controllers;

import com.example.smart_garden.entities.SymptomEntity;
import com.example.smart_garden.service.PlantService;
import com.example.smart_garden.service.SymptomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reasoning")
@CrossOrigin(origins = "*")
public class ReasoningController {

    private final PlantService plantService;
    private final SymptomService symptomService;

    public ReasoningController(PlantService plantService, SymptomService symptomService) {
        this.plantService = plantService;
        this.symptomService = symptomService;
    }

    @GetMapping("/{plantName}")
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
    }

    @PostMapping("/{plantName}/symptoms")
    public ResponseEntity<Void> addSymptom(
            @AuthenticationPrincipal UserDetails userDetails,  // 👈 добавено
            @PathVariable String plantName,
            @RequestBody Map<String, String> payload) {
        String symptom = payload.get("name"); // ключът може да е "name" или както решиш
        plantService.addSymptom(plantName, symptom);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{plantName}/symptoms")
    public List<SymptomEntity> getSymptoms(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String plantName) {
        return symptomService.getSymptomsByPlant(plantName);
    }

    @DeleteMapping("/{plantName}/symptoms/{symptomId}")
    public ResponseEntity<?> deleteSymptom(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String plantName,
            @PathVariable Long symptomId) {
        symptomService.deleteSymptomById(symptomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/symptom-options")
    public Map<String, List<String>> getSymptomOptions(@AuthenticationPrincipal UserDetails userDetails) {
        return plantService.getSymptomOptionsGrouped();
    }
}
