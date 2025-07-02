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

    private final Map<String, PlantAgent> plantAgents = new ConcurrentHashMap<>();

    public void setContainer(ContainerController container) {
        this.container = container;
    }

    public void registerPlantAgent(String plantName, PlantAgent agent) {
        plantAgents.put(plantName, agent);

    }

    public void unregisterPlantAgent(String plantName) {
        plantAgents.remove(plantName);

    }

    public PlantAgent getPlantAgent(String plantName) {
        return plantAgents.get(plantName);
    }

    public void startSensorAgent(Long plantId) {
        if (container == null) return;

        String agentName = "SensorAgent-" + plantId;

        try {
            jade.wrapper.AgentController existingAgent = container.getAgent(agentName);

            return;
        } catch (Exception ignored) {
        }

        try {
            container.createNewAgent(
                    agentName,
                    SensorAgent.class.getName(),
                    new Object[]{plantId}
            ).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void startPlantAgent(Long plantId, String plantName, String plantType) {
        if (getPlantAgent(plantName) != null) {

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

        } catch (Exception e) {
            System.err.println("Грешка при стартиране на PlantAgent за " + plantName);
            e.printStackTrace();
        }
    }


    public void triggerReasoningFor(Long plantId, String plantName, String plantType) {
        PlantAgent agent = getPlantAgent(plantName);
        if (agent != null) {
            agent.doReasoning();
        } else {
            startPlantAgent(plantId, plantName, plantType);
        }
    }


    public void stopAgentsForPlant(Long plantId, String plantName) {
        try {
            String sensorAgentName = "SensorAgent-" + plantId;
            String plantAgentName = "PlantAgent-" + plantName;

            container.getAgent(sensorAgentName).kill();
            container.getAgent(plantAgentName).kill();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Премахване от регистрите (за всеки случай)
        unregisterPlantAgent(plantName);
    }
}
