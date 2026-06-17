import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Map;
import java.util.TreeMap;

public class RunwayAgent extends Agent {

    private final Map<Integer, String> schedule = new TreeMap<>();

    protected void setup() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    String[] parts   = msg.getContent().split(",");
                    String flightId  = parts[0];
                    int    slotTime  = Integer.parseInt(parts[1]);
                    String operation = parts[2];

                    ACLMessage reply = msg.createReply();

                    if (!schedule.containsKey(slotTime)) {
                        schedule.put(slotTime, flightId);
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        reply.setContent("ACCEPTED," + slotTime);
                        // Output identique au code original
                        System.out.println("Runway schedule: " + schedule);
                    } else {
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        reply.setContent("REJECTED," + slotTime);
                    }
                    send(reply);
                } else {
                    block();
                }
            }
        });
    }
}