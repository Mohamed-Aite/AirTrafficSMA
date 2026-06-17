import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;

public class FlightAgent extends Agent {

    protected void setup() {
        Object[] args = getArguments();
        final String flightId = getLocalName();
        final String op       = (String) args[0];
        final int    time     = Integer.parseInt(args[1].toString());
        final int    priority = Integer.parseInt(args[2].toString());

        addBehaviour(new jade.core.behaviours.WakerBehaviour(this, 500) {
            protected void onWake() {
                ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                req.addReceiver(new AID("controller", AID.ISLOCALNAME));
                req.setContent(flightId + "," + op + "," + time + "," + priority);
                send(req);
            }
        });

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    System.out.println(getLocalName() + " received: " + msg.getContent());
                } else {
                    block();
                }
            }
        });
    }
}