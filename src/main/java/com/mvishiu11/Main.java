package com.mvishiu11;

import com.mvishiu11.agents.MarketAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static final ExecutorService jadeExecutor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        final Runtime runtime = Runtime.instance();
        final Profile profile = new ProfileImpl();
        profile.setParameter("gui", "true");

        try {
            // Create Main Container
            final ContainerController mainContainer = jadeExecutor.submit(() -> runtime.createMainContainer(profile)).get();

            // Create an order for the client
            List<String> orderItems = new ArrayList<>();
            orderItems.add("milk");
            orderItems.add("coffee");
            orderItems.add("rice");

            // Create and start ClientAgent
            final AgentController clientAgent = mainContainer.createNewAgent(
                    "ClientAgent",
                    "com.mvishiu11.agents.ClientAgent",
                    new Object[]{ orderItems }
            );

            // Create and start delivery agents
            final AgentController deliveryAgent1 = mainContainer.createNewAgent(
                    "DeliveryAgent1",
                    "com.mvishiu11.agents.DeliveryAgent",
                    new Object[]{ "Bolt", 5.0, 0L }
            );

            final AgentController deliveryAgent2 = mainContainer.createNewAgent(
                    "DeliveryAgent2",
                    "com.mvishiu11.agents.DeliveryAgent",
                    new Object[]{ "UberEats", 5.0, 0L }
            );

            // Create inventories for MarketAgents
            Map<String, Double> inventory1 = new HashMap<>();
            inventory1.put("milk", 5.0);
            inventory1.put("coffee", 30.0);
            Map<String, Double> inventory2 = new HashMap<>();
            inventory2.put("coffee", 25.0);
            inventory2.put("rice", 3.0);
            Map<String, Double> inventory3 = new HashMap<>();
            inventory3.put("milk", 4.0);
            Map<String, Double> inventory4 = new HashMap<>();
            inventory4.put("milk", 16.0);
            inventory4.put("coffee", 28.0);

            // Create and start MarketAgents
            final AgentController marketAgent1 = mainContainer.createNewAgent(
                    "MarketAgent1",
                    "com.mvishiu11.agents.MarketAgent",
                    new Object[]{ "Market1", inventory1 }
            );
            final AgentController marketAgent2 = mainContainer.createNewAgent(
                    "MarketAgent2",
                    "com.mvishiu11.agents.MarketAgent",
                    new Object[]{ "Market2", inventory2 }
            );

            final AgentController marketAgent3 = mainContainer.createNewAgent(
                    "MarketAgent3",
                    "com.mvishiu11.agents.MarketAgent",
                    new Object[]{ "Market3", inventory3, "DeliveryAgent2" }
            );
            final AgentController marketAgent4 = mainContainer.createNewAgent(
                    "MarketAgent4",
                    "com.mvishiu11.agents.MarketAgent",
                    new Object[]{ "Market4", inventory4 }
            );

            // Start all agents
            clientAgent.start();
            deliveryAgent1.start();
            deliveryAgent2.start();
            marketAgent1.start();
            marketAgent2.start();
            marketAgent3.start();
            marketAgent4.start();

            System.out.println("All agents started!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
