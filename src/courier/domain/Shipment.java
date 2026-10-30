package courier.domain;

public class Shipment {
    private final String guide;
    private final String destination;
    private final ShipmentType type;
    private final int receivedMinute;
    private int attempts;
    private int waitingSinceMinute;

    public Shipment(String guide, String destination, ShipmentType type, int receivedMinute) {
        this.guide = guide;
        this.destination = destination;
        this.type = type;
        this.receivedMinute = receivedMinute;
        this.attempts = 0;
        this.waitingSinceMinute = receivedMinute;
    }

    public String getGuide() {
        return guide;
    }

    public String getDestination() {
        return destination;
    }

    public ShipmentType getType() {
        return type;
    }

    public int getReceivedMinute() {
        return receivedMinute;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getWaitingSinceMinute() {
        return waitingSinceMinute;
    }

    public void registerAttempt() {
        attempts++;
    }

    public void waitFrom(int minute) {
        waitingSinceMinute = minute;
    }
}
