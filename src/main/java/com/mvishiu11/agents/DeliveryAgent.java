package com.mvishiu11.agents;

import com.mvishiu11.behaviours.delivery.DeliveryBehaviour;
import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import java.util.Arrays;
import java.util.List;

public class DeliveryAgent extends Agent {
    private String deliveryServiceName;
    private double deliveryFee;
    private long visualizationDelay;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length >= 3) {
            deliveryServiceName = (String) args[0];
            deliveryFee = (double) args[1];
            visualizationDelay = (args[2] instanceof Number) ? ((Number) args[2]).longValue() : 0;
        } else {
            deliveryServiceName = "UnknownDelivery";
            deliveryFee = 0.0;
            visualizationDelay = 0;
        }
        System.out.println(getLocalName() + ": Starting with service " + deliveryServiceName +
                ", fee = " + deliveryFee + ", delay = " + visualizationDelay);

        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("delivery-service");
            sd.setName(deliveryServiceName);
            dfd.addServices(sd);
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        addBehaviour(new DeliveryBehaviour(this, deliveryFee, visualizationDelay));
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
