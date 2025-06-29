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

@Service
public class AgentManagerService {

    private ContainerController container;

    public void setContainer(ContainerController container) {
        this.container = container;
    }


    public void startSensorAgent(Long plantId) {
        if (container == null) return;
        String agentName = "SensorAgent-" + plantId;
        try {
            container.createNewAgent(
                    "SensorAgent-" + plantId,
                    "com.example.smart_garden.agents.SensorAgent",
                    new Object[]{plantId}
            ).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startPlantAgent(Long plantId, String plantName) {
        String agentName = "PlantAgent-" + plantName;
        try {
            container.createNewAgent(
                    "PlantAgent-" + plantId,
                    PlantAgent.class.getName(),
                    new Object[]{plantName}
            ).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopAgentsForPlant(Long plantId, String plantName) {
        try {
            String sensorAgentName = "SensorAgent-" + plantId;
            String plantAgentName = "PlantAgent-" + plantName;

            System.out.println(" Спиране на агенти за растение " + plantName);

            container.getAgent(sensorAgentName).kill();
            container.getAgent(plantAgentName).kill();

        } catch (Exception e) {
            System.err.println(" Грешка при спиране на агентите за растение " + plantName);
            e.printStackTrace();
        }
    }
}
