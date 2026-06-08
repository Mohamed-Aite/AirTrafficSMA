# Air Traffic Slot Allocation Multi-Agent System (AirTrafficSMA)

## Overview

**AirTrafficSMA** is a multi-agent system (MAS) implementation for automated air traffic slot allocation at airports. The system assigns landing and takeoff slots in fixed 5-minute intervals, ensuring conflict-free scheduling while respecting flight priorities (emergency, low fuel, normal). The project is modeled using **GAIA** and **AUML** methodologies and implemented in **Java** using the **JADE framework**.

Air traffic management at busy airports requires efficient coordination to allocate runway time slots to arriving and departing aircraft, prevent scheduling conflicts (two aircraft cannot use the same slot), prioritize flights based on operational urgency (emergency landings, low fuel, etc.), and minimize delays while maintaining fairness. This system demonstrates how multi-agent technology can solve this coordination challenge through autonomous agents communicating via the FIPA (Foundation for Intelligent Physical Agents) protocol.

## Architecture & Design

### System Components

The system consists of three primary agent roles:

**Flight Agent (FlightAgent.java)**: Serves as the flight requester role. It submits landing/takeoff requests with flight ID, operation type, desired time, and priority. Flight agents receive and process assigned slot notifications and autonomously manage flight state based on allocation outcomes. They communicate by sending REQUEST messages to the Controller and receiving INFORM messages with slot assignments. Example instantiation: Flight "F1" requesting to LAND at minute 10 with Emergency priority (1).

**Controller Agent (ControllerAgent.java)**: Acts as the central coordinator and slot scheduler. It maintains a queue of incoming flight slot requests and prioritizes flights using a two-level policy: Priority level (1=Emergency, 2=Low Fuel, 3=Normal) followed by Requested time (earlier preferred). The controller proposes available slots to the Runway Agent, notifies flights of accepted allocations, and retries postponed requests on rejection. It receives REQUEST messages from Flight Agents, sends PROPOSE messages to the Runway Agent, receives ACCEPT_PROPOSAL or REJECT_PROPOSAL responses, and sends INFORM messages back to selected Flight Agents. The controller automatically rounds requested times up to the nearest 5-minute boundary (e.g., 12 → 15 minutes).

**Runway Agent (RunwayAgent.java)**: Functions as the runway resource manager. It maintains a schedule mapping slot times to assigned flights, verifies slot availability before confirming allocations, and prevents double-booking by rejecting proposals for occupied slots. The runway agent receives PROPOSE messages from the Controller and sends back ACCEPT_PROPOSAL or REJECT_PROPOSAL responses. It uses a Map<Integer, String> data structure to map slotTime → flightId.

**FlightRequest (FlightRequest.java)**: A data transfer object encapsulating flight request parameters including flightId (unique flight identifier), operation ("LAND" or "TAKEOFF"), requestedTime (desired slot time in minutes), and priority (priority level 1=Emergency, 2=Low Fuel, 3=Normal).

**Slot (Slot.java)**: Represents an allocated runway slot with time (slot time in minutes) and flightId (associated flight).

### Interaction Protocol

The system uses a Contract Net-like slot allocation protocol where: (1) Flight sends REQUEST(flightId, operation, time, priority) to Controller, (2) Controller sends PROPOSE(flightId, slotTime) to Runway, (3) Runway responds with ACCEPT_PROPOSAL or REJECT_PROPOSAL to Controller, and (4) Controller sends INFORM(flightId, assignedSlot) to Flight.

## JADE Framework Integration

The implementation leverages the JADE (Java Agent Development Framework) for agent lifecycle management, messaging, and container orchestration. Runtime & Containers use Runtime, ProfileImpl, and AgentContainer for distributed agent deployment. All agents extend jade.core.Agent as the base class. Behaviours include CyclicBehaviour for continuous message-driven actions in Controller, Runway, and Flight listeners, and OneShotBehaviour for one-time actions like initial flight request submission. ACL Messaging uses performatives REQUEST, PROPOSE, ACCEPT_PROPOSAL, REJECT_PROPOSAL, and INFORM. Agent Identifiers (AID) enable local-name routing. Message Templates and blocking receive provide synchronization. Configuration files include APDescription.txt for Agent Platform description and MTPs-Main-Container.txt for Message transport protocol endpoints.

## Slot Allocation Algorithm

The controller implements a priority-based queuing with backoff algorithm. The process works as follows: (1) RECEIVE: Flight request → Queue, (2) SORT: Queue by (priority ASC, requestedTime ASC), (3) SELECT: Top request from sorted queue, (4) NORMALIZE: Round requested time up to nearest 5-minute slot, (5) PROPOSE: Send slot allocation proposal to Runway, (6) WAIT: Block until Runway response, (7) IF accepted - Remove request from queue, Notify flight with INFORM message, Log successful allocation; ELSE (rejected) - Increment requested time by 5 minutes, Request remains in queue for next iteration, (8) LOOP: Return to step 2.

### Example Execution Flow

Given flights with priorities and requested times:
- F1: LAND at 10 min, Emergency (priority 1)
- F2: TAKEOFF at 12 min, Normal (priority 3)
- F3: LAND at 11 min, Low Fuel (priority 2)
- F4: LAND at 10 min, Normal (priority 3)
- F5: TAKEOFF at 10 min, Emergency (priority 1)

Processing order (sorted by priority then time): F1, F5, F3, F2, F4

Allocated slots:
- F1 → slot 10
- F5 → slot 15 (conflict at 10, so next available)
- F3 → slot 20
- F2 → slot 15 (conflict, retries incrementally)
- F4 → slot 25

## GAIA Methodology Mapping

Though explicit GAIA artifacts are not present as separate documents, the implementation adheres to GAIA role modeling. The Flight Requester role is instantiated by multiple FlightAgents with permissions to send slot requests and responsibilities to submit requests with priority and respond to allocations. The Scheduler/Coordinator role is the single ControllerAgent with permissions for queue management and slot proposal and responsibilities to maintain request queue, apply prioritization policy, and coordinate with runway. The Resource Manager role is the single RunwayAgent with permissions to accept/reject allocations and manage capacity and responsibilities to track slot occupancy and enforce single-use constraint per slot.

The interaction model employs a request-propose-response interaction protocol where Flight Agent initiates requests for resources, Controller Agent coordinates, and Runway Agent validates. Services include the Slot Allocation Service provided by Controller and Runway agents to assign conflict-free runway times and the Request Queuing Service provided by Controller to manage inbound flight requests.

## AUML Sequence Diagrams

### Standard Allocation Sequence
```
FlightAgent          ControllerAgent         RunwayAgent
    |                     |                       |
    |------ REQUEST ------->|                      |
    |                       |                      |
    |                       |----- PROPOSE ------->|
    |                       |                      |
    |                       |<- ACCEPT_PROPOSAL ---|
    |                       |                      |
    |<------ INFORM --------|                      |
    |  (slot assigned)      |                      |
```

### Rejection & Retry Sequence
```
FlightAgent          ControllerAgent         RunwayAgent
    |                     |                       |
    |------ REQUEST ------->|                      |
    |                       |                      |
    |                       |----- PROPOSE ------->|
    |                       |                      |
    |                       |<-- REJECT_PROPOSAL --|
    |                       |  (slot occupied)     |
    |                       |                      |
    |      [Retry with      |                      |
    |       +5 min slot]    |                      |
    |                       |----- PROPOSE ------->|
    |                       |                      |
    |                       |<- ACCEPT_PROPOSAL ---|
    |                       |                      |
    |<------ INFORM --------|                      |
```

## Installation & Setup

### Prerequisites
- Java 8 or later
- JADE Framework (included in project configuration)
- IntelliJ IDEA or any Java IDE (optional but recommended)

### Building the Project

Clone the repository: `git clone https://github.com/Mohamed-Aite/AirTrafficSMA.git` and `cd AirTrafficSMA`

Compile Java source files: `javac -cp lib/jade.jar src/*.java`

Run the simulation: `java -cp lib/jade.jar:src Main`

### Configuration

The system uses hardcoded agent initialization in Main.java. To modify the scenario (add/remove flights, change priorities), edit Main.java lines 19-23:
```java
mc.createNewAgent("F1", "FlightAgent", new Object[]{"F1","LAND","10","1"}).start();
// Parameters: agentName, agentClass, {flightId, operation, requestedTime, priority}
```

Priority values are: 1 = Emergency, 2 = Low Fuel, 3 = Normal.

## Execution Output

When run, the system produces console output showing slot proposal submissions to the runway, slot acceptance/rejection decisions, final slot assignments to flights, and runway schedule state. Sample output:
```
Assigned F1 -> slot 10 (priority 1)
Runway schedule: {10=F1}
Assigned F5 -> slot 15 (priority 1)
Runway schedule: {10=F1, 15=F5}
...
F1 received: SLOT_ASSIGNED,10
F5 received: SLOT_ASSIGNED,15
...
```

## Project Structure

```
AirTrafficSMA/
├── README.md                 # Project documentation
├── AirTrafficSMA.iml        # IntelliJ module configuration
├── APDescription.txt        # JADE platform description
├── MTPs-Main-Container.txt  # JADE message transport protocol endpoints
└── src/
    ├── Main.java            # JADE container bootstrap and scenario setup
    ├── FlightAgent.java     # Flight requester role implementation
    ├── ControllerAgent.java # Slot scheduler coordinator role
    ├── RunwayAgent.java     # Runway resource manager role
    ├── FlightRequest.java   # Flight request data model
    └── Slot.java            # Slot allocation data model
```

## Design Patterns & Best Practices

The implementation follows key design patterns: Agent Autonomy where each agent manages its own state and behaviors independently, Asynchronous Communication using JADE's ACL messaging for decoupled, event-driven interaction, Priority-Based Scheduling ensuring critical flights (emergency, low fuel) receive precedence, Conflict Resolution where the Runway agent enforces single-use constraint and the controller handles retries, and Modular Roles providing clear separation of concerns (request, coordination, resource management).

## Known Limitations & Future Enhancements

### Current Limitations
- **Hardcoded Scenario**: Flights must be predefined in Main.java; no dynamic flight addition at runtime
- **Single Runway**: Only one runway resource; no support for multiple parallel runways
- **Linear Backoff**: Failed requests increment by fixed 5-minute intervals (could be optimized)
- **Operation Type Unused**: "LAND" vs "TAKEOFF" distinction captured but not used in scheduling
- **No Time Bounds**: No maximum wait time or deadline handling
- **Blocking Controller Loop**: Controller's blockingReceive serializes requests (potential bottleneck)

### Potential Extensions
1. **Multiple Runways**: Extend Runway Agent to manage multiple parallel resources with load balancing
2. **Dynamic Flight Injection**: Implement a generator agent creating flights at runtime
3. **Time Horizon Planning**: Runway agent could support lookahead scheduling for better optimization
4. **Deadline Handling**: Add time windows and deadline constraints to flight requests
5. **Performance Metrics**: Collect and report allocation success rates, average wait times, fairness metrics
6. **Negotiation Protocol**: Replace Contract Net with more sophisticated protocols (e.g., auction-based)
7. **Distributed Runway Management**: Shift from centralized controller to decentralized runway clusters

## Technologies & Dependencies

- **Java 8+**: Core programming language
- **JADE 4.x+**: Multi-agent framework (jar dependency in AirTrafficSMA.iml)
- **FIPA (Foundation for Intelligent Physical Agents)**: ACL messaging standard

## License

This project is provided for educational purposes. All rights reserved.

## Team

| Name | Institution |
|------|-------------|
| Ahrarache Anas | FST Tanger — Université Abdelmalek Essaâdi |
| Mohamed Ait Ezzaouite | FST Tanger — Université Abdelmalek Essaâdi |
| Nadir Mounim | FST Tanger — Université Abdelmalek Essaâdi |

## References

- Wooldridge, M., Jennings, N. R., & Kinny, D. (2000). "The GAIA methodology for agent-oriented analysis and design". Autonomous Agents and Multi-Agent Systems, 3(3), 285–312.
- FIPA ACL Message Structure Specification: http://www.fipa.org/specs/fipa00061/
- JADE Documentation: https://jade.tilab.com/
