import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;

public class Main {
    public static void main(String[] args) throws Exception {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter("gui", "true");  // ← Add this line to enable the GUI
        AgentContainer mc = rt.createMainContainer(p);

        // Runway
        mc.createNewAgent("runway", "RunwayAgent", null).start();

        // Controller
        mc.createNewAgent("controller", "ControllerAgent", null).start();

        // Flights (hardcoded)
        mc.createNewAgent("F1", "FlightAgent", new Object[]{"F1","LAND","10","1"}).start();
        mc.createNewAgent("F2", "FlightAgent", new Object[]{"F2","TAKEOFF","12","3"}).start();
        mc.createNewAgent("F3", "FlightAgent", new Object[]{"F3","LAND","11","2"}).start();
        mc.createNewAgent("F4", "FlightAgent", new Object[]{"F4","LAND","10","3"}).start();
        mc.createNewAgent("F5", "FlightAgent", new Object[]{"F5","TAKEOFF","10","1"}).start();
    }
}