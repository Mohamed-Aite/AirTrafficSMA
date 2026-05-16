import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.Map;

public class RunwayAgent extends Agent {
    private Map<Integer, String> schedule = new HashMap<>(); // slotTime -> flightId

    protected void setup() {
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.PROPOSE) {
                    String[] parts = msg.getContent().split(",");
                    String flightId = parts[0];
                    int slotTime = Integer.parseInt(parts[1]);

                    ACLMessage reply = msg.createReply();
                    if (!schedule.containsKey(slotTime)) {
                        schedule.put(slotTime, flightId);
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        reply.setContent("ACCEPTED");

                        // Print current schedule
                        System.out.println("Runway schedule: " + schedule);
                    } else {
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        reply.setContent("REJECTED");
                    }
                    send(reply);
                } else {
                    block();
                }
            }
        });
    }
}