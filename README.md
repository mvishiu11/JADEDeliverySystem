# JADE Grocery Delivery System

This project implements a multi-agent system using [JADE](http://jade.tilab.com/) that simulates a grocery delivery scenario. In this system, a client orders grocery items (e.g., milk, coffee, rice), and one or more delivery agents respond by negotiating with various market agents. Each market agent represents a store with its own inventory and pricing. The delivery agent uses an iterative selection process to combine offers from different markets when no single market can supply all items. The project also supports agent suspension (waiting for a start command) and includes an option to launch the JADE Sniffer for monitoring agent communications.

---

## Table of Contents

- [Features](#features)
- [Project Structure](#project-structure)
- [Agents Overview](#agents-overview)
- [How It Works](#how-it-works)
- [Getting Started](#getting-started)
- [Running the Project](#running-the-project)
- [License](#license)

---

## Features

- **Multi-Agent Communication:** Uses the Contract Net Protocol style for interaction between Client, Delivery, and Market agents.
- **Iterative Market Selection:** The DeliveryAgent aggregates market responses iteratively when no single market can fulfill an order.
- **ContractNet Communication:** The agents communicate via [ContractNet](http://www.fipa.org/specs/fipa00029/index.html) protocol.
---

## Project Structure

The project is organized into the following packages:

- **com.mvishiu11**  
  Contains the `Main.java` that starts the JADE container and launches all agents.

- **com.mvishiu11.agents**  
  Contains custom agents:
    - `ClientAgent`: Initiates the order by sending a CFP (Call for Proposals).
    - `DeliveryAgent`: Responds to the client's CFP. It negotiates with MarketAgents using an iterative selection process and uses a suspension mechanism to start work only when triggered.
    - `MarketAgent`: Represents a market with a specific inventory and pricing.

- **com.mvishiu11.behaviours**  
  Contains behaviors that encapsulate agent logic:
    - **Client Behaviours:**
        - `FindDeliveryAgentsBehaviour`: Looks up available DeliveryAgents from the DF and sends CFPs.
        - `ReceiveProposalsBehaviour`: Aggregates proposals received from DeliveryAgents and selects the best proposal.
        - `PaymentAndConfirmationBehaviour`: Handles payment and confirmation messages.
    - **Delivery Behaviours:**
        - `DeliveryBehaviour`: Handles incoming CFPs and starts the market query process.
        - `MarketQueryBehaviour`: Queries MarketAgents (either via DF or via a provided list), aggregates offers, and iteratively selects the best market responses.
    - **Market Behaviours:**
        - `MarketResponseBehaviour`: Listens for market queries and responds with available items and pricing.

---

## Agents Overview

- **ClientAgent:**  
  Initiates a grocery order (list of items) and sends out a CFP to all DeliveryAgents registered in the Directory Facilitator (DF).

- **DeliveryAgent:**  
  Upon receiving a CFP from the client, a DeliveryAgent queries all available ones via DF to get offers. Its `MarketQueryBehaviour` aggregates responses using an iterative selection algorithm to combine offers from multiple markets if necessary. The agent uses an optional visualization delay to slow down the process for demonstration purposes.
- **MarketAgent:**  
  Each MarketAgent has an inventory (a mapping of items to prices) and responds to market queries from DeliveryAgents with the items it can offer.

- **Sniffer Agent (JADE built-in):**  
  The system can launch the built-in JADE Sniffer to monitor messages exchanged between agents.

---

## How It Works

1. **Client Order:**  
   The ClientAgent starts with an order (e.g., `[milk, coffee, rice]`) and searches the DF for DeliveryAgents.

2. **CFP & Proposal:**  
   The ClientAgent sends a CFP (Call for Proposal) to DeliveryAgents.  
   A DeliveryAgent receives the CFP and, instead of replying immediately, starts a MarketQueryBehaviour.

3. **Market Query & Aggregation:**  
   The DeliveryAgent sends a market query to designated MarketAgents (either via a DF lookup or from a provided list).  
   Each MarketAgent replies with its inventory information (e.g., MarketAgent1 may supply only “rice” and MarketAgent2 supplies “milk” and “coffee”).  
   The DeliveryAgent then uses an iterative algorithm to combine the best offers from different markets until all items are fulfilled.

4. **Proposal to Client:**  
   After aggregating the offers and adding its own delivery fee, the DeliveryAgent sends a proposal back to the ClientAgent.

5. **Selection & Transaction:**  
   The ClientAgent collects proposals from available DeliveryAgents, selects the best one, sends a start/payment command, and awaits confirmation.

6. **Sniffer Integration:**  
   Optionally, the JADE Sniffer can be started to monitor all communications among agents.

---

## Getting Started

### Prerequisites

- **Java:** JDK 23 or higher.
- **JADE Library:** Download [JADE 4.x](http://jade.tilab.com/) and include the JADE JARs in your project classpath.

### Building the Project

The project uses Maven for dependency management, however JADE dependency is defined as local. That means it will be taken directly from the `jade.jar` file in the `lib` directory. Since this dependency is provided with the project, to build the project simply use normal Maven approach.

---

## Running the Project

1. **Launch the Main Container:**  
   Run the `Main.java` file in the `com.mvishiu11` package. This will:
    - Start the JADE container with the built-in GUI.
    - Create and start the ClientAgent, DeliveryAgent, and MarketAgents.
    - Optionally, start the JADE Sniffer.

2. **Observe the Console Logs:**  
   You should see console output showing the progress of the Contract Net Protocol, the iterative market selection process, and the final proposals. If your scenario requires combining offers from multiple markets, ensure that your test inventories reflect this (for example, one market only supplies “rice” while another supplies “milk” and “coffee”).

## License

This project is provided as-is for educational purposes. Feel free to modify and extend it as needed.