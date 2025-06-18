package com.example.smart_garden.service;

import com.example.smart_garden.entities.PlantEntity;
import com.example.smart_garden.entities.SensorReadingEntity;
import com.example.smart_garden.repositories.SensorReadingRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SensorDataService {

    private final SensorReadingRepository sensorRepo;

    // Валидни параметри и нива
    private final List<String> parameters = List.of("temperature", "light", "humidity", "soilMoisture");
    private List<Double> levels = Arrays.asList(20.0, 25.0, 30.0);

    public SensorDataService(SensorReadingRepository sensorRepo) {
        this.sensorRepo = sensorRepo;
    }

    /**
     * Генерира по 1 случайна стойност за всеки параметър
     */
    public void generateRandomReadingsForPlant(PlantEntity plant) {
        for (String param : parameters) {
            SensorReadingEntity reading = new SensorReadingEntity();
            reading.setParameter(param);
            reading.setReadingValue(randomLevel());
            reading.setPlant(plant);
            reading.setTimestamp(java.time.LocalDateTime.now());

            sensorRepo.save(reading);
        }
    }

    /**
     * Връща последните 10 записа за дадено растение
     */
    public List<SensorReadingEntity> getRecentReadingsForPlant(PlantEntity plant) {
        return sensorRepo.findTop10ByPlantOrderByTimestampDesc(plant);
    }

    /**
     * Извлича последните стойности по параметър от списък със записи
     */
    public Map<String, Double> extractLastValues(List<SensorReadingEntity> readings) {
        Map<String, Double> lastValues = new HashMap<>();
        for (SensorReadingEntity r : readings) {
            lastValues.put(r.getParameter(), r.getReadingValue()); // последната (най-скорошна) ще презапише предишната
        }
        return lastValues;
    }

    private double randomLevel() {
        return levels.get(new Random().nextInt(levels.size()));
    }
}
