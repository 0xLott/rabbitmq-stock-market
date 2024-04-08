package exchange;

public abstract class Order {
    private String asset;
    private String broker;
    private int amount;
    private double value;

    public Order(String asset, String broker, int amount, double value) {
        this.asset = asset;
        this.broker = broker;
        this.amount = amount;
        this.value = value;
    }

    public String getAsset() {
        return asset;
    }

    public String getBroker() {
        return broker;
    }

    public int getAmount() {
        return amount;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return  " Ativo: " + asset +
                "\n Broker: " + broker +
                "\n Quantidade: " + amount +
                "\n Valor: " + value;
    }
}