package com.mvishiu11.behaviours.client;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.List;

public class FindDeliveryAgentsBehaviour extends Behaviour {
    private final Agent agent;
    private final List<String> orderItems;
    private boolean isDone = false;
    private final long startTime;
    private final long MAX_WAIT_TIME = 15000; // Maximum time to wait (15 sec)

    public FindDeliveryAgentsBehaviour(Agent agent, List<String> orderItems) {
        super(agent);
        this.agent = agent;
        this.orderItems = orderItems;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void action() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("delivery-service");
        template.addServices(sd);
        try {
            DFAgentDescription[] results = DFService.search(agent, template);
            System.out.println(agent.getLocalName() + ": Found " + results.length + " DeliveryAgents.");
            if (results.length > 0) {
                // Send CFP to all found DeliveryAgents
                for (DFAgentDescription dfd : results) {
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    cfp.addReceiver(dfd.getName());
                    cfp.setConversationId("order-request");
                    cfp.setContent(String.join(",", orderItems));
                    agent.send(cfp);
                    System.out.println(agent.getLocalName() + ": Sent CFP to " + dfd.getName());
                }
                // Add behavior to receive proposals and finish this behavior.
                agent.addBehaviour(new com.mvishiu11.behaviours.client.ReceiveProposalsBehaviour(agent, results.length));
                isDone = true;
            } else {
                // If not found, wait a second and try again until the maximum wait time is reached.
                if (System.currentTimeMillis() - startTime < MAX_WAIT_TIME) {
                    block(1000);
                } else {
                    System.out.println(agent.getLocalName() + ": No DeliveryAgents found after waiting.");
                    isDone = true;
                }
            }
        } catch (FIPAException e) {
            e.printStackTrace();
            isDone = true;
        }
    }

    @Override
    public boolean done() {
        return isDone;
    }
}
