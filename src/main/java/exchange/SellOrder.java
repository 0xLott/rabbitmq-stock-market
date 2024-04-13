package exchange;

import model.Order;

public class SellOrder extends Order {
    public SellOrder(String asset, String broker, int amount, double value) {
        super(asset, broker, amount, value);
    }
}
