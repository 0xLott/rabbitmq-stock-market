package exchange;

import java.util.List;
import java.util.ArrayList;

public class OrderBook {
    private List<Order> orders;

    public OrderBook(List<Order> orders) {
        this.orders = new ArrayList<>(orders);
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
    }

    // TODO public List<Order> searchOrder(String criteria)
}
