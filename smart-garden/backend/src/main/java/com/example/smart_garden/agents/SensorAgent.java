package com.example.smart_garden.agents;

import com.example.smart_garden.config.SpringContextBridge;
import com.example.smart_garden.entities.PlantEntity;
import com.example.smart_garden.entities.SensorReadingEntity;
import com.example.smart_garden.service.PlantService;
import com.example.smart_garden.service.SensorDataService;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.List;
import java.util.Map;

public class SensorAgent extends Agent {

    private SensorDataService sensorDataService;
    private PlantService plantService;
    private PlantEntity plant;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args == null || args.length == 0) {
            System.err.println("SensorAgent: Не е подадено растение!");
            doDelete();
            return;
        }

        Long plantId = (Long) args[0];
        sensorDataService = SpringContextBridge.getBean(SensorDataService.class);
        plantService = SpringContextBridge.getBean(PlantService.class);

        plant = plantService.getPlantById(plantId).orElse(null);
        if (plant == null) {
            System.err.println("SensorAgent: Неуспешно намиране на растение с ID=" + plantId);
            doDelete();
            return;
        }

        addBehaviour(new TickerBehaviour(this, 40000) {
            @Override
            protected void onTick() {
                sensorDataService.generateRandomReadingsForPlant(plant);
                List<SensorReadingEntity> readings = sensorDataService.getRecentReadingsForPlant(plant);

                Map<String, Double> latest = sensorDataService.extractLastValues(readings);

                StringBuilder sb = new StringBuilder();
                sb.append("plantName=").append(plant.getName()).append(",");
                latest.forEach((k, v) -> sb.append(k).append("=").append(v).append(","));
                String content = sb.toString();

                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new jade.core.AID("MonitorAgent", jade.core.AID.ISLOCALNAME));
                msg.setContent(content);
                send(msg);

                System.out.println(getLocalName() + " изпрати: " + content);
            }
        });
    }
}
