package com.example.smart_garden.repositories;

import com.example.smart_garden.entities.PlantEntity;
import com.example.smart_garden.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantRepository extends JpaRepository<PlantEntity, String> {

    List<PlantEntity> findByUser(UserEntity user);
}
