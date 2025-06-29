package com.example.smart_garden.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UIAgent extends Agent {

    public static final Map<String, List<String>> plantAlerts = new ConcurrentHashMap<>();

    @Override
    protected void setup() {
        System.out.println("🖥️ UIAgent стартира.");

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println("📥 UIAgent получи: " + msg.getContent());

                    String[] parts = msg.getContent().split(",");
                    String plantName = "";
                    List<String> risks = new ArrayList<>();

                    for (String part : parts) {
                        if (part.startsWith("plant=")) {
                            plantName = part.substring("plant=".length());
                        } else if (part.startsWith("risks=")) {
                            risks = Arrays.asList(part.substring("risks=".length()).split("\\|"));
                        }
                    }

                    if (!plantName.isEmpty() && !risks.isEmpty()) {
                        plantAlerts.put(plantName, risks);
                        System.out.println("⚠️ Рискове за " + plantName + ": " + risks);
                    }
                } else {
                    block();
                }
            }
        });
    }
}
