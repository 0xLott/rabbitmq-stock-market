package exchange;

import java.util.List;
import java.util.ArrayList;

public class OrderBook {
    private final List<Order> orders;

    public OrderBook(List<Order> orders) {
        this.orders = new ArrayList<>(orders);
    }

    public void addOrder(Order order) {
        orders.add(order);
        System.out.println("\n A seguinte ordem foi adicionada com sucesso ao Livro de Ofertas:\n" + order);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
    }

    public List<Order> searchOrder(String asset, int amount, double value) {
        List<Order> foundOrders = new ArrayList<>();

        for (Order existingOrder : orders) {
            if (existingOrder.getAsset().equals(asset) &&
                existingOrder.getAmount() == amount &&
                existingOrder.getValue() == value
            ) {
                foundOrders.add(existingOrder);
            }
        }

        return foundOrders;
    }
}