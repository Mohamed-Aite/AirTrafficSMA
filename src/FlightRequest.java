public class FlightRequest {
    public String flightId;
    public String operation; // "LAND" or "TAKEOFF"
    public int requestedTime; // minutes since 0
    public int priority; // 1=Emergency, 2=LowFuel, 3=Normal

    public FlightRequest(String id, String op, int time, int priority) {
        this.flightId = id;
        this.operation = op;
        this.requestedTime = time;
        this.priority = priority;
    }
}