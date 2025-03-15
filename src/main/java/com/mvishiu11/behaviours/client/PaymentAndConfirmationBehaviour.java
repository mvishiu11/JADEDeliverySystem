package com.mvishiu11.behaviours.client;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class PaymentAndConfirmationBehaviour extends Behaviour {
    private final Agent agent;
    private final AID winner;
    private int step = 0;
    private boolean isDone = false;

    public PaymentAndConfirmationBehaviour(Agent agent, AID winner) {
        super(agent);
        this.agent = agent;
        this.winner = winner;
    }

    @Override
    public void action() {
        switch (step) {
            case 0:
                ACLMessage payMsg = new ACLMessage(ACLMessage.INFORM);
                payMsg.setConversationId("payment");
                payMsg.addReceiver(winner);
                payMsg.setContent("payment-info");
                agent.send(payMsg);
                System.out.println(agent.getLocalName() + ": Sent payment info to " + winner.getLocalName());
                step++;
                break;
            case 1:
                MessageTemplate mt = MessageTemplate.MatchConversationId("order-completed");
                ACLMessage msg = agent.receive(mt);
                if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                    System.out.println(agent.getLocalName() + ": Order completed by " + msg.getSender().getLocalName());
                    isDone = true;
                } else {
                    block();
                }
                break;
        }
    }

    @Override
    public boolean done() {
        return isDone;
    }
}
