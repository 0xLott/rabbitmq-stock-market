package exchange;

public class BuyOrder extends Order {
    public BuyOrder(String asset, String broker, int amount, double value) {
        super(asset, broker, amount, value);
    }
}
