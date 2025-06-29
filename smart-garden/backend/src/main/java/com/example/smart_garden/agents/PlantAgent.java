package com.example.smart_garden.agents;

import com.example.smart_garden.config.SpringContextBridge;
import com.example.smart_garden.entities.PlantEntity;
import com.example.smart_garden.ontology.PlantOntology;
import com.example.smart_garden.service.AgentManagerService;
import com.example.smart_garden.service.PlantService;
import com.example.smart_garden.service.ReasoningBlock;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlantAgent extends Agent {

    private PlantOntology ontology;
    private String plantName;
    private String plantType;
    private Long plantId;
    private List<ReasoningBlock> latestAdvice = new ArrayList<>();

    private volatile boolean reasoningCompleted = false; // 🔒 за нишки

    public boolean isReasoningCompleted() {
        return reasoningCompleted;
    }

    public void resetReasoningFlag() {
        this.reasoningCompleted = false;
    }

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args == null || args.length < 2) {
            System.err.println("❌ PlantAgent: Липсват аргументи (име и тип)!");
            doDelete();
            return;
        }

        this.plantId = (Long) args[0];
        this.plantName = (String) args[1];
        this.plantType = (String) args[2];

        this.ontology = SpringContextBridge.getBean(PlantOntology.class);

        // 🟢 Регистриране в мениджъра
        AgentManagerService manager = SpringContextBridge.getBean(AgentManagerService.class);
        manager.registerPlantAgent(plantName, this);

        System.out.println("✅ PlantAgent стартиран за растение: " + plantName);

        // Първоначално reasoning изпълнение
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                runReasoning();
            }
        });
    }

    @Override
    protected void takeDown() {
        SpringContextBridge.getBean(AgentManagerService.class).unregisterPlantAgent(plantName);
        System.out.println("🛑 PlantAgent за " + plantName + " приключи.");
    }

    // 🔁 Извикай reasoning от контролер или друго място
    public void doReasoning() {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                runReasoning();
            }
        });
    }

    // 🤖 Изпълнение на reasoning логика
    private void runReasoning() {
        // Вземи PlantEntity от името
        PlantService plantService = SpringContextBridge.getBean(PlantService.class);
        Optional<PlantEntity> plantOpt = plantService.getPlantWithSymptoms(plantId);


        if (plantOpt.isEmpty()) {
            System.err.println("❌ Не може да се зареди растение: " + plantName);
            return;
        }

        PlantEntity plant = plantOpt.get();


        // Вземи всички reasoning блокове за типа
        List<ReasoningBlock> allAdvice = ontology.getAdviceForPlantIndividual(plantType); // или по тип, ако се налага

        // Филтрирай спрямо симптомите
        latestAdvice = allAdvice.stream()
                .filter(block -> block.getSymptoms().stream()
                        .anyMatch(symptom ->
                                plant.getSymptomsStrings().stream().anyMatch(symptom::endsWith)))
                .toList();

        System.out.println("💡 Reasoning за " + plantName + " – резултати: " + latestAdvice.size());

        for (ReasoningBlock block : latestAdvice) {
            System.out.println("  Причина: " + block.getCause());
            System.out.println("  Симптоми: " + block.getSymptoms());
            System.out.println("  Грижи: " + block.getActions());
        }

        reasoningCompleted = true;
    }


    public List<ReasoningBlock> getLatestAdvice() {
        return latestAdvice;
    }

}
