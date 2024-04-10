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
        System.out.println("\nA seguinte ordem foi adicionada com sucesso ao Livro de Ofertas:\n" + order);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
        System.out.println("Ordem removida com sucesso ao Livro de Ofertas!");
    }

    public List<Order> searchOrder(String asset, int amount, double value) {
        List<Order> foundOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.getAsset().equals(asset) && order.getAmount() == amount && order.getValue() == value) {
                foundOrders.add(order);
            }
        }
        return foundOrders;
    }
}