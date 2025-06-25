package com.example.smart_garden.service;

import java.util.ArrayList;
import java.util.List;

public class ReasoningBlock {
    private String cause;
    private List<String> symptoms = new ArrayList<>();
    private List<String> actions = new ArrayList<>();

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }
}
