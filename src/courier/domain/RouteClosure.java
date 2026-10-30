package courier.domain;

public record RouteClosure(
        int delivered,
        int returnedToPending,
        int sentToRetries,
        int pendingSize,
        int retriesSize) {
}
