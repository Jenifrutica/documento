package courier.domain;

public record TraversalRow(
        int position,
        String guide,
        String destination,
        StopState state,
        boolean current) {
}
