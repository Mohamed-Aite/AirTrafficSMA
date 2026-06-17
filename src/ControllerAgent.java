import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ControllerAgent extends Agent {

    private final List<FlightRequest> queue = new ArrayList<>();
    private AID runwayAgent;
    private FlightRequest pending = null;

    private static final MessageTemplate FROM_FLIGHTS =
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST);

    private static final MessageTemplate FROM_RUNWAY =
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
                    MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)
            );

    protected void setup() {
        runwayAgent = new AID("runway", AID.ISLOCALNAME);

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                if (pending == null) {
                    // COLLECTING phase
                    ACLMessage msg;
                    while ((msg = receive(FROM_FLIGHTS)) != null) {
                        String[] p = msg.getContent().split(",");
                        queue.add(new FlightRequest(
                                p[0], p[1],
                                Integer.parseInt(p[2]),
                                Integer.parseInt(p[3])
                        ));
                    }

                    if (queue.isEmpty()) {
                        block(200);
                        return;
                    }

                    queue.sort(Comparator
                            .comparingInt((FlightRequest f) -> f.priority)
                            .thenComparingInt(f -> f.requestedTime));

                    pending = queue.get(0);
                    int slotTime = roundUpTo5(pending.requestedTime);
                    pending.requestedTime = slotTime;

                    ACLMessage propose = new ACLMessage(ACLMessage.PROPOSE);
                    propose.addReceiver(runwayAgent);
                    propose.setContent(pending.flightId + "," + slotTime + "," + pending.operation);
                    send(propose);

                } else {
                    // NEGOTIATING phase
                    ACLMessage reply = receive(FROM_RUNWAY);

                    if (reply == null) {
                        block(50);
                        return;
                    }

                    if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                        queue.remove(pending);

                        ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                        inform.addReceiver(new AID(pending.flightId, AID.ISLOCALNAME));
                        inform.setContent("SLOT_ASSIGNED," + pending.requestedTime);
                        send(inform);

                        // Output identique au code original
                        System.out.println("Assigned " + pending.flightId
                                + " -> slot " + pending.requestedTime
                                + " (priority " + pending.priority + ")");

                        pending = null;

                    } else {
                        pending.requestedTime += 5;

                        ACLMessage propose = new ACLMessage(ACLMessage.PROPOSE);
                        propose.addReceiver(runwayAgent);
                        propose.setContent(pending.flightId + ","
                                + pending.requestedTime + ","
                                + pending.operation);
                        send(propose);
                    }
                }
            }
        });
    }

    private int roundUpTo5(int t) {
        return ((t + 4) / 5) * 5;
    }
}