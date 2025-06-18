package com.example.smart_garden.repositories;

import com.example.smart_garden.entities.SensorReadingEntity;
import com.example.smart_garden.entities.PlantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SensorReadingRepository extends JpaRepository<SensorReadingEntity, Long> {
    List<SensorReadingEntity> findTop10ByPlantOrderByTimestampDesc(PlantEntity plant);
}
