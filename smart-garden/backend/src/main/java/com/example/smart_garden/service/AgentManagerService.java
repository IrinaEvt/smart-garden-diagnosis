package com.example.smart_garden.service;

import com.example.smart_garden.agents.PlantAgent;
import com.example.smart_garden.agents.SensorAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentManagerService {

    private ContainerController container;

    // 🌿 Регистрирани PlantAgent-и по име на растение
    private final Map<String, PlantAgent> plantAgents = new ConcurrentHashMap<>();

    public void setContainer(ContainerController container) {
        this.container = container;
    }

    // ✅ Регистриране на PlantAgent от самия агент в setup()
    public void registerPlantAgent(String plantName, PlantAgent agent) {
        plantAgents.put(plantName, agent);
        System.out.println("📌 Регистриран PlantAgent за: " + plantName);
    }

    // ❌ Премахване от регистъра (от takeDown)
    public void unregisterPlantAgent(String plantName) {
        plantAgents.remove(plantName);
        System.out.println("🗑️ Премахнат PlantAgent за: " + plantName);
    }

    // 🔍 Вземи PlantAgent по име на растение
    public PlantAgent getPlantAgent(String plantName) {
        return plantAgents.get(plantName);
    }

    // 🚀 Стартиране на SensorAgent
    public void startSensorAgent(Long plantId) {
        if (container == null) return;

        String agentName = "SensorAgent-" + plantId;

        try {
            jade.wrapper.AgentController existingAgent = container.getAgent(agentName);
            System.out.println("⚠️ SensorAgent вече съществува: " + agentName);
            return;
        } catch (Exception ignored) {
        }

        try {
            container.createNewAgent(
                    agentName,
                    SensorAgent.class.getName(),
                    new Object[]{plantId}
            ).start();
            System.out.println("🚀 Стартиран SensorAgent: " + agentName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🚀 Стартирай reasoning агент ако не съществува
    public void startPlantAgent(Long plantId, String plantName, String plantType) {
        if (getPlantAgent(plantName) != null) {
            System.out.println("⚠️ PlantAgent вече съществува: " + plantName);
            return;
        }

        try {
            String agentName = "PlantAgent-" + plantName;
            AgentController controller = container.createNewAgent(
                    agentName,
                    PlantAgent.class.getName(),
                    new Object[]{plantId,plantName, plantType}
            );
            controller.start();
            System.out.println("🚀 Стартиран PlantAgent: " + agentName);
        } catch (Exception e) {
            System.err.println("❌ Грешка при стартиране на PlantAgent за " + plantName);
            e.printStackTrace();
        }
    }

    // 🧠 Извикай reasoning, ако агентът е активен
    public void triggerReasoningFor(Long plantId, String plantName, String plantType) {
        PlantAgent agent = getPlantAgent(plantName);
        if (agent != null) {
            agent.doReasoning();  // трябва да го има в PlantAgent
        } else {
            System.out.println("❗ PlantAgent не е стартиран – стартирам сега.");
            startPlantAgent(plantId, plantName, plantType);
        }
    }

    // 🛑 Спри агентите свързани с растение
    public void stopAgentsForPlant(Long plantId, String plantName) {
        try {
            String sensorAgentName = "SensorAgent-" + plantId;
            String plantAgentName = "PlantAgent-" + plantName;

            System.out.println("🛑 Спиране на агенти за растение: " + plantName);

            container.getAgent(sensorAgentName).kill();
            container.getAgent(plantAgentName).kill();

        } catch (Exception e) {
            System.err.println("❌ Грешка при спиране на агентите за " + plantName);
            e.printStackTrace();
        }

        // Премахване от регистрите (за всеки случай)
        unregisterPlantAgent(plantName);
    }
}
