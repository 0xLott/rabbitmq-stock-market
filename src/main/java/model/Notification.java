package model;

public class Notification {
    private final String operation;
    private final String asset;
    private final String amount;
    private final String value;
    private final String broker;

    public Notification(String operation, String asset, String amount, String value, String broker) {
        this.operation = operation;
        this.asset = asset;
        this.broker = broker;
        this.amount = amount;
        this.value = value;
    }

    public String buildMessage() {
        return operation + "." + asset + "<" + amount + ";" + "," + value + ";" + broker + ">";
    }

    public String getBroker() {
        return broker;
    }
}
