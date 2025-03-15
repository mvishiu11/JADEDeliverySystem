package com.mvishiu11.behaviours.market;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Map;

public class MarketResponseBehaviour extends CyclicBehaviour {
    private final Agent agent;
    private final Map<String, Double> inventory;

    public MarketResponseBehaviour(Agent agent, Map<String, Double> inventory) {
        super(agent);
        this.agent = agent;
        this.inventory = inventory;
    }

    @Override
    public void action() {
        ACLMessage msg = agent.receive();
        if (msg != null && "market-query".equals(msg.getConversationId())) {
            if (msg.getPerformative() == ACLMessage.REQUEST) {
                String content = msg.getContent();
                String[] requestedItems = content.split(",");
                StringBuilder responseContent = new StringBuilder();
                for (String item : requestedItems) {
                    item = item.trim().toLowerCase();
                    if (inventory.containsKey(item)) {
                        responseContent.append(item)
                                .append(":")
                                .append(inventory.get(item))
                                .append(",");
                    }
                }
                if (responseContent.length() > 0) {
                    responseContent.setLength(responseContent.length() - 1);
                }
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(responseContent.toString());
                agent.send(reply);
                System.out.println(agent.getLocalName() + ": Replied with inventory info: " + reply.getContent());
            }
        } else {
            block();
        }
    }
}
