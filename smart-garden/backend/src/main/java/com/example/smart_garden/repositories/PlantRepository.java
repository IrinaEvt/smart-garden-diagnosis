package com.example.smart_garden.repositories;

import com.example.smart_garden.entities.PlantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlantRepository extends JpaRepository<PlantEntity, String> {
    // String е типът на @Id (name) в PlantEntity
}
