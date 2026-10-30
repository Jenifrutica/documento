package courier.domain;

public record RouteStopView(
        int position,
        String guide,
        String destination,
        ShipmentType type,
        StopState state,
        boolean current,
        int attempts) {
}
