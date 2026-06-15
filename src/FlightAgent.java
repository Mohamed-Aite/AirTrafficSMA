import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour; // ← Import WakerBehaviour
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class FlightAgent extends Agent {
    protected void setup() {
        Object[] args = getArguments();
        String flightId = (String) args[0];
        String op = (String) args[1];
        int time = Integer.parseInt(args[2].toString());
        int priority = Integer.parseInt(args[3].toString());

        // Replace OneShotBehaviour with WakerBehaviour
        // 15000 = 15 seconds of delay
        addBehaviour(new WakerBehaviour(this, 60000) {
            protected void onWake() {
                ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                req.addReceiver(new AID("controller", AID.ISLOCALNAME));
                req.setContent(flightId + "," + op + "," + time + "," + priority);
                send(req);
            }
        });

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                    System.out.println(getLocalName() + " received: " + msg.getContent());
                } else {
                    block();
                }
            }
        });
    }
}