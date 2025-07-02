package com.example.smart_garden.repositories;

import com.example.smart_garden.entities.SymptomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SymptomRepository extends JpaRepository<SymptomEntity, Long> {

    List<SymptomEntity> findByPlantId(Long id);
}

