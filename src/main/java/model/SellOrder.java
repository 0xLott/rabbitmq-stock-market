package model;

import model.Order;

public class SellOrder extends Order {
    public SellOrder(String operation, String asset, String broker, int amount, double value) {
        super(operation, asset, broker, amount, value);
    }
}
