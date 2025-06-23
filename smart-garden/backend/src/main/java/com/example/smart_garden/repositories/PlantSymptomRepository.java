
package com.example.smart_garden.repositories;


import com.example.smart_garden.entities.PlantInfoSymptomEntity;
import org.springframework.data.jpa.repository.JpaRepository;



public interface PlantSymptomRepository extends JpaRepository<PlantInfoSymptomEntity, Long> {
}
