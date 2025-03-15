package com.mvishiu11.behaviours.delivery;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.*;

public class MarketQueryBehaviour extends Behaviour {
    private final Agent agent;
    private final ACLMessage cfpMessage;
    private final List<String> requestedItems;
    private final double deliveryFee;
    private final long visualizationDelay;
    private int step = 0;
    private long startTime;
    private final long TIMEOUT = 5000; // 5 seconds for market responses
    private final List<ACLMessage> responses = new ArrayList<>();
    private boolean done = false;
    private double aggregatedCost = 0.0;
    private int expectedResponses = 0; // number of expected responses

    public MarketQueryBehaviour(Agent agent, ACLMessage cfpMessage, List<String> requestedItems,
                                double deliveryFee, long visualizationDelay) {
        this.agent = agent;
        this.cfpMessage = cfpMessage;
        this.requestedItems = requestedItems;
        this.deliveryFee = deliveryFee;
        this.visualizationDelay = visualizationDelay;
    }

    @Override
    public void onStart() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void action() {
        switch (step) {
            case 0:
                jade.domain.FIPAAgentManagement.DFAgentDescription template1 = new jade.domain.FIPAAgentManagement.DFAgentDescription();
                jade.domain.FIPAAgentManagement.ServiceDescription sd1 = new jade.domain.FIPAAgentManagement.ServiceDescription();
                sd1.setType("market-service");
                sd1.addProperties(new jade.domain.FIPAAgentManagement.Property("provider", agent.getLocalName()));
                template1.addServices(sd1);
                jade.domain.FIPAAgentManagement.DFAgentDescription[] results1 = null;
                try {
                    results1 = jade.domain.DFService.search(agent, template1);
                } catch (jade.domain.FIPAException fe) {
                    fe.printStackTrace();
                }

                jade.domain.FIPAAgentManagement.DFAgentDescription template2 = new jade.domain.FIPAAgentManagement.DFAgentDescription();
                jade.domain.FIPAAgentManagement.ServiceDescription sd2 = new jade.domain.FIPAAgentManagement.ServiceDescription();
                sd2.setType("market-service");
                sd2.addProperties(new jade.domain.FIPAAgentManagement.Property("provider", "ALL"));
                template2.addServices(sd2);
                jade.domain.FIPAAgentManagement.DFAgentDescription[] results2 = null;
                try {
                    results2 = jade.domain.DFService.search(agent, template2);
                } catch (jade.domain.FIPAException fe) {
                    fe.printStackTrace();
                }

                // Merge the two result sets (avoiding duplicates)
                List<jade.domain.FIPAAgentManagement.DFAgentDescription> combined = new ArrayList<>();
                if (results1 != null) {
                    combined.addAll(Arrays.asList(results1));
                }
                if (results2 != null) {
                    for (jade.domain.FIPAAgentManagement.DFAgentDescription dfd : results2) {
                        boolean found = false;
                        for (jade.domain.FIPAAgentManagement.DFAgentDescription existing : combined) {
                            if (existing.getName().equals(dfd.getName())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            combined.add(dfd);
                        }
                    }
                }

                // Create a request message using the union of DF search results.
                ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                for (jade.domain.FIPAAgentManagement.DFAgentDescription dfd : combined) {
                    req.addReceiver(dfd.getName());
                }
                req.setConversationId("market-query");
                req.setContent(String.join(",", requestedItems));
                agent.send(req);
                // Determine the expected number of responses.
                expectedResponses = combined.size();
                System.out.println(agent.getLocalName() + ": Sent market-query to "
                        + expectedResponses + " MarketAgents with content: " + req.getContent());
                step = 1;
                break;
            case 1:
                // Collect responses until timeout or until expected responses are received.
                MessageTemplate mt = MessageTemplate.and(
                        MessageTemplate.MatchConversationId("market-query"),
                        MessageTemplate.or(
                                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                                MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
                        )
                );
                ACLMessage reply;
                // Drain the message queue.
                while ((reply = agent.receive(mt)) != null) {
                    responses.add(reply);
                    System.out.println(agent.getLocalName() + ": Received market response: " + reply.getContent());
                }
                if (responses.size() >= expectedResponses || System.currentTimeMillis() - startTime > TIMEOUT) {
                    System.out.println(agent.getLocalName() + ": Collected " + responses.size() + " market responses (expected " + expectedResponses + ").");
                    step = 2;
                } else {
                    block(100); // Wait a bit before checking again.
                }
                break;
            case 2:
                // Iteratively compute the aggregated cost from the collected responses.
                aggregatedCost = computeIterativeAggregatedCost();
                step = 3;
                break;
            case 3:
                // Send reply to the original CFP.
                ACLMessage replyMsg = cfpMessage.createReply();
                if (aggregatedCost < 0) {
                    replyMsg.setPerformative(ACLMessage.REFUSE);
                    replyMsg.setContent("Cannot fulfill order");
                    System.out.println(agent.getLocalName() + ": Cannot fulfill order, sending REFUSE.");
                } else {
                    double finalCost = aggregatedCost + deliveryFee;
                    replyMsg.setPerformative(ACLMessage.PROPOSE);
                    replyMsg.setContent(String.valueOf(finalCost));
                    System.out.println(agent.getLocalName() + ": Sending PROPOSE with final cost " + finalCost);
                }
                agent.send(replyMsg);
                done = true;
                break;
        }
    }

    private double computeIterativeAggregatedCost() {
        // Build market offers from responses.
        List<Map<String, Double>> marketOffers = new ArrayList<>();
        for (ACLMessage msg : responses) {
            if (msg.getPerformative() == ACLMessage.INFORM) {
                Map<String, Double> offer = new HashMap<>();
                String content = msg.getContent(); // e.g., "milk:6.0,coffee:28.0" or "rice:4.0"
                String[] pairs = content.split(",");
                for (String pair : pairs) {
                    String[] keyValue = pair.split(":");
                    if (keyValue.length == 2) {
                        String item = keyValue[0].trim().toLowerCase();
                        double price = Double.parseDouble(keyValue[1].trim());
                        offer.put(item, price);
                    }
                }
                marketOffers.add(offer);
            }
        }

        // Remaining items
        List<String> remainingItems = new ArrayList<>();
        for (String item : requestedItems) {
            remainingItems.add(item.trim().toLowerCase());
        }
        double total = 0.0;

        while (!remainingItems.isEmpty()) {
            int bestCount = 0;
            double bestPrice = Double.MAX_VALUE;
            Map<String, Double> bestOffer = null;

            // Evaluate each market offer.
            for (Map<String, Double> offer : marketOffers) {
                List<String> available = new ArrayList<>();
                double priceSum = 0.0;
                for (String item : remainingItems) {
                    if (offer.containsKey(item)) {
                        available.add(item);
                        priceSum += offer.get(item);
                    }
                }
                int count = available.size();
                // Choose the offer that supplies the most items; tie-break by lower total price.
                if (count > bestCount || (count == bestCount && count > 0 && priceSum < bestPrice)) {
                    bestCount = count;
                    bestPrice = priceSum;
                    bestOffer = offer;
                }
            }

            if (bestCount == 0) {
                System.out.println(agent.getLocalName() + ": Unable to supply items " + remainingItems);
                return -1;
            }
            total += bestPrice;
            System.out.println(agent.getLocalName() + ": Selected market offer covering " + bestCount
                    + " items at cost " + bestPrice);

            // Remove items provided by the selected offer.
            Iterator<String> iter = remainingItems.iterator();
            while (iter.hasNext()) {
                String item = iter.next();
                if (bestOffer.containsKey(item)) {
                    iter.remove();
                }
            }
            try {
                Thread.sleep(visualizationDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return total;
    }

    @Override
    public boolean done() {
        return done;
    }
}
