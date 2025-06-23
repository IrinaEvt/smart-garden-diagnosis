// PlantFamilyRepository.java
package com.example.smart_garden.repositories;

import com.example.smart_garden.entities.PlantFamilyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantFamilyRepository extends JpaRepository<PlantFamilyEntity, Long> {}
