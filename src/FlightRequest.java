/**
 * Represents a flight's request for a runway slot.
 * Holds all metadata needed by the ControllerAgent for scheduling.
 */
public class FlightRequest {
    public String flightId;
    public String operation; // "LAND" or "TAKEOFF"
    public int requestedTime; // minutes since midnight
    public int priority;      // 1=Emergency, 2=LowFuel, 3=Normal

    public FlightRequest(String id, String op, int time, int priority) {
        this.flightId = id;
        this.operation = op;
        this.requestedTime = time;
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "[" + flightId + " | " + operation + " | t=" + requestedTime + " | p=" + priority + "]";
    }
}