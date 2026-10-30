package courier.domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class OperationLog {
    private final List<LogEntry> entries = new ArrayList<>();

    public void add(String operation, boolean success, String detail) {
        entries.add(new LogEntry(LocalTime.now(), operation, success, detail));
    }

    public List<LogEntry> entries() {
        return List.copyOf(entries);
    }
}
