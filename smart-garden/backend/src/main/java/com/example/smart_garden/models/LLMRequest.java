package com.example.smart_garden.models;

public class LLMRequest {

    private String model;
    private String url;
    private String apiKey;
    private String plantName;
    private String image;

    public LLMRequest(String model, String url, String apiKey, String plantName, String image) {
        this.model = model;

        this.url = url;
        this.apiKey = apiKey;
        this.plantName = plantName;
        this.image = image;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
