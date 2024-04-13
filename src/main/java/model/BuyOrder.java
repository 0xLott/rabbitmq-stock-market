package model;

import model.Order;

public class BuyOrder extends Order {
    public BuyOrder(String operation, String asset, String broker, int amount, double value) {
        super(operation, asset, broker, amount, value);
    }
}