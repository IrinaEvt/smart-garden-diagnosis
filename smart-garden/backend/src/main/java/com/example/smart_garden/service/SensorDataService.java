package com.example.smart_garden.service;

import com.example.smart_garden.entities.PlantEntity;
import com.example.smart_garden.entities.SensorReadingEntity;
import com.example.smart_garden.repositories.SensorReadingRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SensorDataService {

    private final SensorReadingRepository sensorRepo;


    private final List<String> parameters = List.of("temperature", "light", "humidity", "soilMoisture");


    public SensorDataService(SensorReadingRepository sensorRepo) {
        this.sensorRepo = sensorRepo;
    }


    public void generateRandomReadingsForPlant(PlantEntity plant) {
        for (String param : parameters) {
            double value = generateRandomValueForParam(param);

            SensorReadingEntity reading = new SensorReadingEntity();
            reading.setParameter(param);
            reading.setReadingValue(value);
            reading.setPlant(plant);
            reading.setTimestamp(java.time.LocalDateTime.now());

            sensorRepo.save(reading);
            System.out.printf("✅ Записано: %s = %.2f за растение %s%n", param, value, plant.getName());
        }
    }



    public List<SensorReadingEntity> getRecentReadingsForPlant(PlantEntity plant) {
        List<SensorReadingEntity> result = new ArrayList<>();
        for (String param : parameters) {
            List<SensorReadingEntity> perParam = sensorRepo
                    .findTop10ByPlantAndParameterOrderByTimestampDesc(plant, param);
            result.addAll(perParam);
        }
        return result;
    }


    public Map<String, Double> extractLastValues(List<SensorReadingEntity> readings) {
        Map<String, SensorReadingEntity> latestPerParam = new HashMap<>();

        for (SensorReadingEntity reading : readings) {
            String param = reading.getParameter();
            if (!latestPerParam.containsKey(param) ||
                    reading.getTimestamp().isAfter(latestPerParam.get(param).getTimestamp())) {
                latestPerParam.put(param, reading);
            }
        }

        Map<String, Double> lastValues = new HashMap<>();
        latestPerParam.forEach((param, reading) -> lastValues.put(param, reading.getReadingValue()));
        return lastValues;
    }


    public List<String> evaluateAlertsFromMap(Map<String, Double> values) {
        List<String> alerts = new ArrayList<>();

        for (Map.Entry<String, Double> entry : values.entrySet()) {
            String param = entry.getKey();
            double value = entry.getValue();

            switch (param) {
                case "temperature":
                    if (value < 20) alerts.add("Ниска температура: " + value);
                    else if (value > 35) alerts.add("Висока температура: " + value);
                    break;
                case "light":
                    if (value < 500) alerts.add("Слаба светлина: " + value);
                    else if (value > 500) alerts.add("Силна светлина: " + value);
                    break;
                case "humidity":
                    if (value < 60) alerts.add("Ниска влажност: " + value);
                    else if (value > 60) alerts.add("Висока влажност: " + value);
                    break;
                case "soilMoisture":
                    if (value < 40) alerts.add("Суха почва: " + value);
                    else if (value > 60) alerts.add("Прекалено влажна почва: " + value);
                    break;
            }
        }

        return alerts;
    }





    private double generateRandomValueForParam(String param) {
        Random rand = new Random();
        double chance = rand.nextDouble(); // 0.0 - 1.0

        double value;

        switch (param) {
            case "temperature": {
                int normalMin = 20;
                int normalMax = 35;

                if (chance < 0.7) {
                    value = normalMin + rand.nextDouble() * (normalMax - normalMin);
                } else if (chance < 0.85) {
                    value = normalMin - rand.nextDouble() * 10; // under 20
                } else {
                    value = normalMax + rand.nextDouble() * 10; // over 35
                }
                break;
            }

            case "light": {
                int normalMin = 300;
                int normalMax = 700;
                if (chance < 0.7) {
                    value = normalMin + rand.nextDouble() * (normalMax - normalMin);
                } else if (chance < 0.85) {
                    value = normalMin - rand.nextDouble() * 200; // under 300
                } else {
                    value = normalMax + rand.nextDouble() * 300; // over 700
                }
                break;
            }

            case "humidity": {
                int normalMin = 50;
                int normalMax = 70;
                if (chance < 0.7) {
                    value = normalMin + rand.nextDouble() * (normalMax - normalMin);
                } else if (chance < 0.85) {
                    value = normalMin - rand.nextDouble() * 20;
                } else {
                    value = normalMax + rand.nextDouble() * 20;
                }
                break;
            }

            case "soilMoisture": {
                int normalMin = 40;
                int normalMax = 70;
                if (chance < 0.7) {
                    value = normalMin + rand.nextDouble() * (normalMax - normalMin);
                } else if (chance < 0.85) {
                    value = normalMin - rand.nextDouble() * 20;
                } else {
                    value = normalMax + rand.nextDouble() * 20;
                }
                break;
            }

            default:
                value = 0;
        }

        return Math.round(value);
    }



}
