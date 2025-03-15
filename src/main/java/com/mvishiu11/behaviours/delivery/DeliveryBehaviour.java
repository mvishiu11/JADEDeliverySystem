package com.mvishiu11.behaviours.delivery;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Arrays;
import java.util.List;

public class DeliveryBehaviour extends CyclicBehaviour {
    private final Agent agent;
    private final double deliveryFee;
    private final long visualizationDelay;

    public DeliveryBehaviour(Agent agent, double deliveryFee, long visualizationDelay) {
        super(agent);
        this.agent = agent;
        this.deliveryFee = deliveryFee;
        this.visualizationDelay = visualizationDelay;
    }

    @Override
    public void action() {
        ACLMessage msg = agent.receive();
        if (msg != null) {
            switch (msg.getPerformative()) {
                case ACLMessage.CFP:
                    System.out.println(agent.getLocalName() + ": Received CFP from "
                            + msg.getSender().getLocalName() + " with content: " + msg.getContent());
                    handleCFP(msg);
                    break;
                case ACLMessage.ACCEPT_PROPOSAL:
                    System.out.println(agent.getLocalName() + ": Proposal accepted by "
                            + msg.getSender().getLocalName());
                    break;
                case ACLMessage.REJECT_PROPOSAL:
                    System.out.println(agent.getLocalName() + ": Proposal rejected by "
                            + msg.getSender().getLocalName());
                    break;
                case ACLMessage.INFORM:
                    if ("payment".equals(msg.getConversationId())) {
                        System.out.println(agent.getLocalName() + ": Received payment info from "
                                + msg.getSender().getLocalName());
                        handlePayment(msg);
                    }
                    break;
                default:
                    break;
            }
        } else {
            block();
        }
    }

    private void handleCFP(ACLMessage msg) {
        List<String> itemsRequested = Arrays.asList(msg.getContent().split(","));
        // Delegate the market conversation to another behaviour
        agent.addBehaviour(new MarketQueryBehaviour(agent, msg, itemsRequested, deliveryFee, visualizationDelay));
    }

    private void handlePayment(ACLMessage msg) {
        ACLMessage inform = msg.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        inform.setConversationId("order-completed");
        inform.setContent("Order completed");
        agent.send(inform);
        System.out.println(agent.getLocalName() + ": Sent order completion confirmation to "
                + msg.getSender().getLocalName());
    }
}
