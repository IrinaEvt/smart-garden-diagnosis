package com.example.smart_garden.entities;


import jakarta.persistence.*;

@Entity
@Table(name = "plant_info_symptoms")
public class PlantInfoSymptomEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symptom_name", nullable = false)
    private String symptomName;

    @Column(name = "part_affected", nullable = false)
    private String partAffected; // Leaf, Stem, Root

    @Column(name = "symptom_type", nullable = false)
    private String symptomType;

    @Column(name = "visual_description", columnDefinition = "TEXT")
    private String visualDescription;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "severity")
    private String severity; // Low, Medium, High

    @Column(name = "is_contagious")
    private Boolean isContagious;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymptomName() {
        return symptomName;
    }

    public void setSymptomName(String symptomName) {
        this.symptomName = symptomName;
    }

    public String getPartAffected() {
        return partAffected;
    }

    public void setPartAffected(String partAffected) {
        this.partAffected = partAffected;
    }

    public String getSymptomType() {
        return symptomType;
    }

    public void setSymptomType(String symptomType) {
        this.symptomType = symptomType;
    }

    public String getVisualDescription() {
        return visualDescription;
    }

    public void setVisualDescription(String visualDescription) {
        this.visualDescription = visualDescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Boolean getContagious() {
        return isContagious;
    }

    public void setContagious(Boolean contagious) {
        isContagious = contagious;
    }
}
