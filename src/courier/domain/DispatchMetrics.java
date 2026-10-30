package courier.domain;

import java.util.ArrayList;
import java.util.List;

public class DispatchMetrics {
    private int received;
    private int assigned;
    private int delivered;
    private int returned;
    private int failed;
    private int reorganizations;
    private int invalidations;
    private int reloads;
    private int sentToRetries;
    private final List<WaitRecord> waits = new ArrayList<>();

    public void shipmentReceived() {
        received++;
    }

    public void shipmentAssigned() {
        assigned++;
    }

    public void shipmentDelivered() {
        delivered++;
    }

    public void shipmentsReturned(int amount) {
        returned += amount;
    }

    public void deliveryFailed() {
        failed++;
    }

    public void reorganizationMovements(int amount) {
        reorganizations += amount;
    }

    public void loadInvalidated() {
        invalidations++;
    }

    public void trunkLoaded() {
        reloads++;
    }

    public void shipmentSentToRetries() {
        sentToRetries++;
    }

    public void addWait(String guide, int minutes) {
        waits.add(new WaitRecord(guide, Math.max(0, minutes)));
    }

    public double averageWait() {
        if (waits.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (WaitRecord record : waits) {
            total += record.minutes();
        }
        return (double) total / waits.size();
    }

    public int maximumWait() {
        int maximum = 0;
        for (WaitRecord record : waits) {
            maximum = Math.max(maximum, record.minutes());
        }
        return maximum;
    }

    public MetricsSnapshot snapshot() {
        return new MetricsSnapshot(
                received,
                assigned,
                delivered,
                returned,
                failed,
                reorganizations,
                invalidations,
                reloads,
                sentToRetries,
                averageWait(),
                maximumWait(),
                List.copyOf(waits));
    }
}
