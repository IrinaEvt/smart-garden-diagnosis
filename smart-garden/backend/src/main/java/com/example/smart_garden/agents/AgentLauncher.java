package com.example.smart_garden.agents;

import com.example.smart_garden.config.SpringContextBridge;
import com.example.smart_garden.ontology.PlantOntology;
import com.example.smart_garden.service.AgentManagerService;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class AgentLauncher implements ApplicationListener<ContextRefreshedEvent> {

    private boolean initialized = false;
    private ContainerController container;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!initialized) {
            if (SpringContextBridge.getApplicationContext() != null) {
                startJade();
                initialized = true;
            } else {
                System.err.println(" SpringContextBridge.context все е null – агентите няма да се стартират.");
            }
        }
    }

    public void startJade() {
        System.out.println("✅ AgentLauncher стартира.");
        try {
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.GUI, "true");
            container = Runtime.instance().createMainContainer(profile);

            PlantOntology ontology = SpringContextBridge.getBean(PlantOntology.class);
            AgentManagerService agentManager = SpringContextBridge.getBean(AgentManagerService.class);

            agentManager.setContainer(container);
            System.out.println("✅ Контейнер зададен на AgentManager");

            startAgent("MonitorAgent", MonitorAgent.class.getName(), new Object[]{ontology});
            startAgent("UIAgent", UIAgent.class.getName(), null);

        } catch (Exception e) {
            System.err.println("❌ Грешка при стартиране на JADE контейнера или агенти:");
            e.printStackTrace();
        }
    }

    private void startAgent(String name, String className, Object[] args) {
        try {
            AgentController agent = container.createNewAgent(name, className, args);
            agent.start();
            System.out.println("✅ Стартиран агент: " + name);
        } catch (Exception e) {
            System.err.println("❌ Грешка при стартиране на агент: " + name);
            e.printStackTrace();
        }
    }
}
