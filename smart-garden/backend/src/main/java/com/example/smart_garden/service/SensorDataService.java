package com.example.smart_garden.service;

import com.example.smart_garden.entities.PlantEntity;
import com.example.smart_garden.entities.SensorReadingEntity;
import com.example.smart_garden.ontology.PlantOntology;
import com.example.smart_garden.repositories.SensorReadingRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SensorDataService {

    private final SensorReadingRepository sensorRepo;

    private final PlantOntology plantOntology;

    public SensorDataService(SensorReadingRepository sensorRepo, PlantOntology plantOntology) {
        this.sensorRepo = sensorRepo;
        this.plantOntology = plantOntology;
    }


    private final List<String> parameters = List.of("temperature", "light", "humidity", "soilMoisture");





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


    public List<String> evaluateAlertsFromMap(Map<String, Double> values, String plantType) {
        List<String> alerts = new ArrayList<>();
        Map<String, String> needs = plantOntology.getNeedsFromPlantType(plantType);

        for (Map.Entry<String, Double> entry : values.entrySet()) {
            String param = entry.getKey();
            double value = entry.getValue();

            if (!needs.containsKey(param)) continue;

            String needLevel = needs.get(param);
            String condition = plantOntology.determineCondition(param, needLevel, value);

            if (condition != null) {
                alerts.add(getReadableAlert(param, condition, value));
            }
        }

        return alerts;
    }


    private String getReadableAlert(String param, String condition, double value) {
        switch (condition) {
            case "LowTemperatureCondition": return "Ниска температура: " + value;
            case "HighTemperatureCondition": return "Висока температура: " + value;
            case "LowHumidityCondition": return "Ниска влажност: " + value;
            case "HighHumidityCondition": return "Висока влажност: " + value;
            case "LowLightCondition": return "Слаба светлина: " + value;
            case "HighLightCondition": return "Силна светлина: " + value;
            case "LowSoilMoistureCondition": return "Суха почва: " + value;
            case "HighSoilMoistureCondition": return "Прекалено влажна почва: " + value;
            default: return param + ": " + value;
        }
    }







    private double generateRandomValueForParam(String param) {
        Random rand = new Random();
        double chance = rand.nextDouble();

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
