package com.example.smart_garden.controllers;

import com.example.smart_garden.agents.PlantAgent;
import com.example.smart_garden.entities.PlantEntity;
import com.example.smart_garden.entities.SymptomEntity;
import com.example.smart_garden.repositories.PlantRepository;
import com.example.smart_garden.service.AgentManagerService;
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
    private AgentManagerService agentManagerService;


    public ReasoningController(PlantService plantService, SymptomService symptomService, PlantRepository plantRepo, AgentManagerService agentManagerService) {
        this.plantRepo = plantRepo;
        this.plantService = plantService;
        this.symptomService = symptomService;
        this.agentManagerService = agentManagerService;
    }

    @GetMapping("/{plantId}/agent")
    public ResponseEntity<Map<String, Object>> getReasoningFromAgent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId) {

        Optional<PlantEntity> plantOpt = plantService.getPlantById(plantId);
        if (plantOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PlantEntity plant = plantOpt.get();
        PlantAgent agent = agentManagerService.getPlantAgent(plant.getName());

        if (agent == null) {
            return ResponseEntity.status(500).body(Map.of("error", "Агентът не е активен."));
        }

        // ❗ Reset-ни флага преди reasoning
        agent.resetReasoningFlag();
        agent.doReasoning();

        List<ReasoningBlock> fullReasoning = new ArrayList<>();
        int retries = 20;

        // ⏳ Изчакай докато reasoning е завършен
        while (retries-- > 0) {
            if (agent.isReasoningCompleted()) {
                fullReasoning = agent.getLatestAdvice();
                break;
            }
            try {
                Thread.sleep(300); // по-добре от 200
            } catch (InterruptedException ignored) {}
        }

        if (fullReasoning.isEmpty()) {
            return ResponseEntity.status(504).body(Map.of("error", "Reasoning timeout – no data from agent"));
        }

        System.out.println("📤 Изпращам reasoning блокове към frontend: " + fullReasoning.size());
        fullReasoning.forEach(rb -> {
            System.out.println("- " + rb.getCause() + " | " + rb.getSymptoms() + " | " + rb.getActions());
        });

        return ResponseEntity.ok(Map.of("reasoning", fullReasoning));
    }





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


  /*  @PostMapping("/{plantId}/symptoms")
    public ResponseEntity<Void> addSymptom(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId,
            @RequestBody Map<String, String> payload) {
        String symptom = payload.get("name");
        plantService.addSymptom(plantId, symptom);
        return ResponseEntity.ok().build();
    }*/

    @PostMapping("/{plantId}/symptoms")
    public ResponseEntity<Void> addSymptom(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long plantId,
            @RequestBody Map<String, String> payload) {

        String symptom = payload.get("name");
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
