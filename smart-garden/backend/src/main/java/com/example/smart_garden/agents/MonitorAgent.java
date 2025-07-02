package com.example.smart_garden.agents;

import com.example.smart_garden.ontology.PlantOntology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitorAgent extends Agent {

    private PlantOntology ontology;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof PlantOntology) {
            ontology = (PlantOntology) args[0];
            System.out.println(getLocalName() + " ✅ стартира успешно.");
        } else {
            System.err.println("❌ Неуспешно подаване на PlantOntology към MonitorAgent.");
            doDelete();
            return;
        }

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String[] tokens = msg.getContent().split(",");
                    String plantName = "";
                    Map<String, Double> values = new HashMap<>();

                    for (String token : tokens) {
                        String[] pair = token.split("=");
                        if (pair[0].equals("plantName")) {
                            plantName = pair[1];
                        } else {
                            try {
                                values.put(pair[0], Double.parseDouble(pair[1]));
                            } catch (NumberFormatException e) {
                                System.err.println("❗ Невалидна стойност за " + pair[0]);
                            }
                        }
                    }

                    if (!plantName.isBlank()) {
                        ontology.evaluatePlantState(plantName, values);

                        List<String> risks = ontology.getRisksForPlant(plantName);
                        System.out.println("Оценени рискове за " + plantName + ": " + risks);

                        if (!risks.isEmpty()) {
                            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                            reply.setContent("plant=" + plantName + ",risks=" + String.join("|", risks));
                            reply.addReceiver(new AID("UIAgent", AID.ISLOCALNAME));
                            send(reply);
                            System.out.println(" Изпратено към UIAgent: " + reply.getContent());

                            ACLMessage riskMsg = new ACLMessage(ACLMessage.INFORM);
                            riskMsg.setContent("RISK_PRESENT");
                            riskMsg.addReceiver(new AID("PlantAgent-" + plantName, AID.ISLOCALNAME));
                            send(riskMsg);
                            System.out.println("Изпратено към PlantAgent-" + plantName + ": RISK_PRESENT");
                        }

                    } else {
                        System.err.println("❌ Липсва plantName в съобщението");
                    }

                } else {
                    block();
                }
            }
        });


    }


}
