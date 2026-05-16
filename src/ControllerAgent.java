import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public class ControllerAgent extends Agent {
    private List<FlightRequest> queue = new ArrayList<>();
    private AID runwayAgent;

    protected void setup() {
        runwayAgent = new AID("runway", AID.ISLOCALNAME);

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                // Receive new requests
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.REQUEST) {
                    String[] p = msg.getContent().split(",");
                    FlightRequest fr = new FlightRequest(
                            p[0], p[1], Integer.parseInt(p[2]), Integer.parseInt(p[3])
                    );
                    queue.add(fr);
                }

                if (!queue.isEmpty()) {
                    // Sort by priority then requestedTime
                    queue.sort(Comparator
                            .comparingInt((FlightRequest f) -> f.priority)
                            .thenComparingInt(f -> f.requestedTime));

                    FlightRequest top = queue.get(0);
                    int slotTime = roundUpTo5(top.requestedTime);

                    ACLMessage propose = new ACLMessage(ACLMessage.PROPOSE);
                    propose.addReceiver(runwayAgent);
                    propose.setContent(top.flightId + "," + slotTime);
                    send(propose);

                    // Wait only for runway response
                    MessageTemplate mt = MessageTemplate.MatchSender(runwayAgent);
                    ACLMessage reply = blockingReceive(mt);

                    if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                        queue.remove(top);

                        // Notify flight
                        ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                        inform.addReceiver(new AID(top.flightId, AID.ISLOCALNAME));
                        inform.setContent("SLOT_ASSIGNED," + slotTime);
                        send(inform);

                        System.out.println("Assigned " + top.flightId +
                                " -> slot " + slotTime + " (priority " + top.priority + ")");
                    } else {
                        // If rejected, push request to next slot (+5)
                        top.requestedTime += 5;
                    }
                } else {
                    block(200);
                }
            }
        });
    }

    private int roundUpTo5(int t) {
        return ((t + 4) / 5) * 5;
    }
}