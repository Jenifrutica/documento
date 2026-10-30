package courier.domain;

import java.util.List;

public record MetricsSnapshot(
        int received,
        int assigned,
        int delivered,
        int returned,
        int failed,
        int reorganizations,
        int invalidations,
        int reloads,
        int sentToRetries,
        double averageWait,
        int maximumWait,
        List<WaitRecord> waits) {
}
