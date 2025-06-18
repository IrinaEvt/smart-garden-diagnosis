package com.example.smart_garden.repositories;

import com.example.smart_garden.entities.SymptomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SymptomRepository extends JpaRepository<SymptomEntity, Long> {

    // Извлича всички симптоми по растение
    List<SymptomEntity> findByPlantName(String plantName);

    // Изтрива симптоми по растение
    void deleteByPlant_Name(String plantName);
}

