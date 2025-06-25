package com.example.smart_garden.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "plant_families_detailed")
public class PlantFamilyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "scientific_name")
    private String scientificName;

    @Column(name = "general_description", columnDefinition = "TEXT")
    private String generalDescription;

    @Column(name = "environment", columnDefinition = "TEXT")
    private String environment;

    @Column(name = "leaf_type")
    private String leafType;

    @Column(name = "root_type")
    private String rootType;

    @Column(name = "flower_description", columnDefinition = "TEXT")
    private String flowerDescription;

    @Column(name = "toxicity")
    private String toxicity;

    @Column(name = "common_genera", columnDefinition = "TEXT")
    private String commonGenera;

    @Column(name = "notable_species", columnDefinition = "TEXT")
    private String notableSpecies;

    @Column(name = "care_light")
    private String careLight;

    @Column(name = "care_watering")
    private String careWatering;

    @Column(name = "care_temperature")
    private String careTemperature;

    @Column(name = "care_soil")
    private String careSoil;

    @Column(name = "common_pests")
    private String commonPests;

    @Column(name = "common_diseases")
    private String commonDiseases;

    @Column(name = "image_url")
    private String imageUrl;

    // Гетъри и сетъри

    public Long getId() {
        return id;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getGeneralDescription() {
        return generalDescription;
    }

    public void setGeneralDescription(String generalDescription) {
        this.generalDescription = generalDescription;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getLeafType() {
        return leafType;
    }

    public void setLeafType(String leafType) {
        this.leafType = leafType;
    }

    public String getRootType() {
        return rootType;
    }

    public void setRootType(String rootType) {
        this.rootType = rootType;
    }

    public String getFlowerDescription() {
        return flowerDescription;
    }

    public void setFlowerDescription(String flowerDescription) {
        this.flowerDescription = flowerDescription;
    }

    public String getToxicity() {
        return toxicity;
    }

    public void setToxicity(String toxicity) {
        this.toxicity = toxicity;
    }

    public String getCommonGenera() {
        return commonGenera;
    }

    public void setCommonGenera(String commonGenera) {
        this.commonGenera = commonGenera;
    }

    public String getNotableSpecies() {
        return notableSpecies;
    }

    public void setNotableSpecies(String notableSpecies) {
        this.notableSpecies = notableSpecies;
    }

    public String getCareLight() {
        return careLight;
    }

    public void setCareLight(String careLight) {
        this.careLight = careLight;
    }

    public String getCareWatering() {
        return careWatering;
    }

    public void setCareWatering(String careWatering) {
        this.careWatering = careWatering;
    }

    public String getCareTemperature() {
        return careTemperature;
    }

    public void setCareTemperature(String careTemperature) {
        this.careTemperature = careTemperature;
    }

    public String getCareSoil() {
        return careSoil;
    }

    public void setCareSoil(String careSoil) {
        this.careSoil = careSoil;
    }

    public String getCommonPests() {
        return commonPests;
    }

    public void setCommonPests(String commonPests) {
        this.commonPests = commonPests;
    }

    public String getCommonDiseases() {
        return commonDiseases;
    }

    public void setCommonDiseases(String commonDiseases) {
        this.commonDiseases = commonDiseases;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
