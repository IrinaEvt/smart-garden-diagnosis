package com.example.smart_garden.controllers;



import com.example.smart_garden.models.LLMRequest;
import com.example.smart_garden.ontology.PlantOntology;
import com.example.smart_garden.service.PlantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/llm")
@CrossOrigin
public class LLMController {
    private String userPrompt;
    private String systemInstruction;
    private final PlantService plantService;

    public LLMController(PlantService plantService) {
        this.plantService = plantService;
    }



    @PostMapping("/health")
    public ResponseEntity<?> health(@RequestBody LLMRequest body) {

        if (body.getApiKey() == null || body.getApiKey().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Missing API key.");
        }

        if (body.getImage() == null || body.getImage().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Image is required.");
        }

        List<String> leafSymptoms = plantService.getAllLeafSymptoms();
        String symptomList = String.join(", ", leafSymptoms);

        RestTemplate restTemplate = new RestTemplate();

        systemInstruction = """
            You are an expert plant pathologist assistant. Your role is to analyze plant images for visual symptoms, cross-reference them with a known list, and return only accurate, confident results. Avoid speculation. Output must be clean and structured.
        """;

        String symptomListJson;
        try {
            symptomListJson = new ObjectMapper().writeValueAsString(symptomList);
        } catch (Exception e){
            symptomListJson = symptomList;
        }
        userPrompt = String.format("""
            This is an image of a plant of type: '%s'.
        
            Here is a list of known symptoms for this plant:
            %s
        
            Your task is:
            1. Analyze the provided image carefully (leaves, stem, color, patterns, etc.).
            2. Compare visual evidence with each symptom in the list.
            3. Determine which symptoms are clearly visible and match the description.
            4. Respond **only with the exact symptom names** from the list that you confidently detect.
            5. Format your output as a valid array, e.g.: ["symptom1", "symptom2"]
            6. Do **not** include any extra text, commentary, or explanation.
        
            If no symptoms are confidently observed, return an empty JSON array: []
        
            Proceed step by step in your reasoning before outputting the final answer.
        """, body.getPlantName(), symptomListJson);

        List<Map<String, Object>> messages = List.of(
                Map.of("role", "system", "content", systemInstruction),
                Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of("type", "text", "text", userPrompt),
                                Map.of("type", "image_url", "image_url", Map.of("url", body.getImage()))
                        )
                )
        );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", body.getModel());
        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(body.getApiKey());
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    body.getUrl(),
                    request,
                    Map.class
            );

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                String content = message.get("content").toString().toLowerCase();
                List<String> matchedSymptoms = leafSymptoms.stream()
                        .filter(symptom -> content.contains(symptom.toLowerCase()))
                        .toList();

                return ResponseEntity.ok(matchedSymptoms);
            } else {
                return ResponseEntity.status(502).body("No response from OpenAI.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
