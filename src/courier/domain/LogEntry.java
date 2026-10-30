package courier.domain;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record LogEntry(
        LocalTime time,
        String operation,
        boolean success,
        String detail) {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public String formattedTime() {
        return time.format(FORMAT);
    }
}
