package com.example.smart_garden.agents;

import com.example.smart_garden.config.SpringContextBridge;
import com.example.smart_garden.entities.PlantEntity;
import com.example.smart_garden.ontology.PlantOntology;
import com.example.smart_garden.service.AgentManagerService;
import com.example.smart_garden.service.PlantService;
import com.example.smart_garden.service.ReasoningBlock;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.*;
import java.util.stream.Collectors;

public class PlantAgent extends Agent {

    private PlantOntology ontology;
    private String plantName;
    private String plantType;
    private Long plantId;

    private List<ReasoningBlock> latestAdvice = new ArrayList<>();
    private volatile boolean reasoningCompleted = false;
    private volatile boolean externalRiskDetected = false;

    public boolean isReasoningCompleted() {
        return reasoningCompleted;
    }

    public void resetReasoningFlag() {
        this.reasoningCompleted = false;
    }

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args == null || args.length < 3) {
            System.err.println("❌ PlantAgent: Липсват аргументи (ID, име и тип)!");
            doDelete();
            return;
        }

        this.plantId = (Long) args[0];
        this.plantName = (String) args[1];
        this.plantType = (String) args[2];

        this.ontology = SpringContextBridge.getBean(PlantOntology.class);

        // Регистрация при AgentManager
        AgentManagerService manager = SpringContextBridge.getBean(AgentManagerService.class);
        manager.registerPlantAgent(plantName, this);

        System.out.println("✅ PlantAgent стартиран за растение: " + plantName);

        // Първоначален reasoning
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                runReasoning();
            }
        });

        // Поведение за получаване на рискови съобщения
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if ("RISK_PRESENT".equals(msg.getContent())) {
                        externalRiskDetected = true;
                        System.out.println("⚠️ PlantAgent " + plantName + ": получено съобщение за външен риск.");
                    }
                } else {
                    block();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        SpringContextBridge.getBean(AgentManagerService.class).unregisterPlantAgent(plantName);
        System.out.println("🛑 PlantAgent за " + plantName + " приключи.");
    }

    public void doReasoning() {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                runReasoning();
            }
        });
    }

    private void runReasoning() {
        PlantService plantService = SpringContextBridge.getBean(PlantService.class);
        Optional<PlantEntity> plantOpt = plantService.getPlantWithSymptoms(plantId);

        if (plantOpt.isEmpty()) {
            System.err.println("❌ Не може да се зареди растение: " + plantName);
            return;
        }

        PlantEntity plant = plantOpt.get();
        List<String> plantSymptoms = plant.getSymptomsStrings();

        List<ReasoningBlock> allAdvice = ontology.getAdviceForPlantIndividual(plantType);

        // Environmental причини
        Set<String> environmentalCauses = Set.of(
                "HighHumidity",
                "HighLight",
                "HighTemperature",
                "LowHumidity",
                "LowLight",
                "LowTemperature",
                "Overwatering",
                "WaterDeficiency"
        );

        // Филтриране по симптоми
        List<ReasoningBlock> filtered = allAdvice.stream()
                .filter(block -> block.getSymptoms().stream()
                        .anyMatch(symptom -> plantSymptoms.stream().anyMatch(symptom::endsWith)))
                .collect(Collectors.toList());

        // Приоритизиране, ако има външен риск
        if (externalRiskDetected) {
            System.out.println("🌡️ Външен риск засечен – приоритизиране на environmental причини...");
            filtered = filtered.stream()
                    .sorted((b1, b2) -> {
                        boolean b1IsEnv = environmentalCauses.contains(b1.getCause());
                        boolean b2IsEnv = environmentalCauses.contains(b2.getCause());

                        System.out.println("🔍 Сравнявам:");
                        System.out.println("  b1: " + b1.getCause() + " (environmental? " + b1IsEnv + ")");
                        System.out.println("  b2: " + b2.getCause() + " (environmental? " + b2IsEnv + ")");

                        int result = Boolean.compare(!b1IsEnv, !b2IsEnv);
                        if (result < 0) {
                            System.out.println("👉 " + b1.getCause() + " ще бъде преди " + b2.getCause());
                        } else if (result > 0) {
                            System.out.println("👉 " + b2.getCause() + " ще бъде преди " + b1.getCause());
                        } else {
                            System.out.println("👉 Без промяна в реда.");
                        }

                        return result;
                    })
                    .collect(Collectors.toList());
        }

        this.latestAdvice = filtered;

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
