import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;

public class Main {
    public static void main(String[] args) throws Exception {
        Runtime rt = Runtime.instance();
        Profile p  = new ProfileImpl();
        p.setParameter(Profile.GUI, "true");
        AgentContainer mc = rt.createMainContainer(p);

        mc.createNewAgent("runway",     "RunwayAgent",     null).start();
        mc.createNewAgent("controller", "ControllerAgent", null).start();

        Thread.sleep(800);

        mc.createNewAgent("sniffer", "jade.tools.sniffer.Sniffer",
                new Object[]{"F1;F2;F3;F4;F5;controller;runway"}).start();

        Thread.sleep(1500);

        mc.createNewAgent("F1", "FlightAgent", new Object[]{"LAND",    "10", "1"}).start();
        mc.createNewAgent("F2", "FlightAgent", new Object[]{"TAKEOFF", "12", "3"}).start();
        mc.createNewAgent("F3", "FlightAgent", new Object[]{"LAND",    "11", "2"}).start();
        mc.createNewAgent("F4", "FlightAgent", new Object[]{"LAND",    "10", "3"}).start();
        mc.createNewAgent("F5", "FlightAgent", new Object[]{"TAKEOFF", "10", "1"}).start();
    }
}