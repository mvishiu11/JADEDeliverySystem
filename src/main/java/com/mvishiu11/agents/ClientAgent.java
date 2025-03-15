package com.mvishiu11.agents;

import com.mvishiu11.behaviours.client.FindDeliveryAgentsBehaviour;
import jade.core.Agent;

import java.util.ArrayList;
import java.util.List;

public class ClientAgent extends Agent {
    private List<String> orderItems;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof List) {
            orderItems = (List<String>) args[0];
        } else {
            orderItems = new ArrayList<>();
            orderItems.add("milk");
            orderItems.add("coffee");
            orderItems.add("rice");
        }
        System.out.println(getLocalName() + ": Starting with order: " + orderItems);
        addBehaviour(new FindDeliveryAgentsBehaviour(this, orderItems));
    }
}
