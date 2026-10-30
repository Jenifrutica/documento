package courier.domain;

public record ShipmentView(
        String guide,
        String destination,
        ShipmentType type,
        int receivedMinute,
        int attempts) {
}
