package courier.domain;

public class DispatchException extends RuntimeException {
    private final String rule;

    public DispatchException(String rule, String message) {
        super(message);
        this.rule = rule;
    }

    public String getRule() {
        return rule;
    }
}
