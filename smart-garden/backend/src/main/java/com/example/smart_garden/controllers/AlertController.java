package com.example.smart_garden.controllers;

import com.example.smart_garden.agents.UIAgent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @GetMapping
    public Map<String, java.util.List<String>> getPlantAlerts() {
        return UIAgent.plantAlerts;
    }
}
