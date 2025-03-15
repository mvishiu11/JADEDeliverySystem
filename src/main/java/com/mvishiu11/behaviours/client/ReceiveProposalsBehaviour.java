package com.mvishiu11.behaviours.client;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;

public class ReceiveProposalsBehaviour extends Behaviour {
    private final Agent agent;
    private final int responsesExpected;
    private int responsesReceived = 0;
    private final List<ACLMessage> proposals = new ArrayList<>();
    private boolean isDone = false;

    public ReceiveProposalsBehaviour(Agent agent, int responsesExpected) {
        super(agent);
        this.agent = agent;
        this.responsesExpected = responsesExpected;
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchConversationId("order-request");
        ACLMessage msg = agent.receive(mt);
        if (msg != null) {
            responsesReceived++;
            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                proposals.add(msg);
                System.out.println(agent.getLocalName() + ": Received PROPOSE with cost " + msg.getContent());
            } else if (msg.getPerformative() == ACLMessage.REFUSE) {
                System.out.println(agent.getLocalName() + ": Received REFUSE from " + msg.getSender().getLocalName());
            }
            if (responsesReceived >= responsesExpected) {
                chooseBestProposal();
                isDone = true;
            }
        } else {
            block();
        }
    }

    private void chooseBestProposal() {
        if (proposals.isEmpty()) {
            System.out.println(agent.getLocalName() + ": No valid proposals received.");
            return;
        }
        double bestPrice = Double.MAX_VALUE;
        ACLMessage bestProposal = null;
        for (ACLMessage proposal : proposals) {
            double cost = Double.parseDouble(proposal.getContent());
            if (cost < bestPrice) {
                bestPrice = cost;
                bestProposal = proposal;
            }
        }
        System.out.println(agent.getLocalName() + ": Chose proposal from " + bestProposal.getSender().getLocalName() + " with cost " + bestPrice);
        // Send ACCEPT_PROPOSAL to the best and REJECT_PROPOSAL to others.
        for (ACLMessage proposal : proposals) {
            ACLMessage reply = proposal.createReply();
            if (proposal.equals(bestProposal)) {
                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            } else {
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
            }
            agent.send(reply);
        }
        // Add behaviour for payment and confirmation.
        agent.addBehaviour(new PaymentAndConfirmationBehaviour(agent, bestProposal.getSender()));
    }

    @Override
    public boolean done() {
        return isDone;
    }
}
