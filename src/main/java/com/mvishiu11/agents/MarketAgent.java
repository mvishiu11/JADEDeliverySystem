package com.mvishiu11.agents;

import com.mvishiu11.behaviours.market.MarketResponseBehaviour;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;

import java.util.HashMap;
import java.util.Map;

public class MarketAgent extends Agent {
    private String marketName;
    private Map<String, Double> inventory;
    private String provider;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            marketName = (String) args[0];
        } else {
            marketName = "UnknownMarket";
        }
        if (args != null && args.length > 1 && args[1] instanceof Map) {
            inventory = (Map<String, Double>) args[1];
        } else {
            inventory = new HashMap<>();
            inventory.put("milk", 5.0);
            inventory.put("coffee", 30.0);
            inventory.put("rice", 4.0);
        }
        if (args != null && args.length > 2) {
            provider = (String) args[2];
        } else {
            provider = "ALL";
        }
        System.out.println(getLocalName() + ": Starting with inventory " + inventory + ", providing for: " + provider);

        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("market-service");
            sd.setName(marketName);
            sd.addProperties(new jade.domain.FIPAAgentManagement.Property("provider", provider));
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        addBehaviour(new MarketResponseBehaviour(this, inventory));
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        System.out.println(getLocalName() + ": Terminating.");
    }
}
