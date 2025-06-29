package com.example.smart_garden.agents;

import com.example.smart_garden.config.SpringContextBridge;
import com.example.smart_garden.ontology.PlantOntology;
import com.example.smart_garden.service.PlantService;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class PlantAgent extends Agent {

    private PlantOntology ontology;
    private String plantName;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args == null || args.length == 0) {
            System.err.println("PlantAgent: Не е подадено име на растение!");
            doDelete();
            return;
        }

        plantName = (String) args[0];
        ontology = SpringContextBridge.getBean(PlantOntology.class);

        addBehaviour(new TickerBehaviour(this, 10000) {
            @Override
            protected void onTick() {
                var adviceList = ontology.getAdviceForPlantIndividual(plantName);

                if (adviceList.isEmpty()) {
                    System.out.println("[" + plantName + "] Няма нови симптоми или препоръки.");
                } else {
                    System.out.println("\n[" + plantName + "] Препоръки за грижа:");
                    for (var block : adviceList) {
                        System.out.println("\nПричина: " + block.getCause());
                        System.out.println("Симптоми: " + String.join(", ", block.getSymptoms()));
                        System.out.println("Грижи: " + String.join(", ", block.getActions()));
                    }
                }
            }
        });
    }
}