package market;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import exchange.*;
import model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OrderMessageHandler {
    private static final TransactionBook transactionBook = new TransactionBook(new ArrayList<>());
    private static final OrderBook orderBook = new OrderBook(new ArrayList<>());
    private static final Object orderBookLock = new Object();

    public static DeliverCallback createDeliverCallback(Channel channel) {

        // Parameters `consumerTag` and `delivery` are defined by the DeliverCallback functional interface
        return (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            String routingKey = delivery.getEnvelope().getRoutingKey();

            try {
                System.out.println(" [x] Received '" + message + "' on topic '" + routingKey + "'");
                handle(message, channel);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println(" [x] Done");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
    }

    private static void handle(String task, Channel channel) throws InterruptedException, IOException {
        String[] parts = splitTask(task);
        String operation = parts[0];
        String asset = parts[1];

        String[] parameters = parts[2].split(";");
        String amount = parameters[0];
        String value = parameters[1];
        String broker = parameters[2];

        value = value.replace(",", ".");

        if (operation == null || asset == null || parameters.length != 3)
            System.out.println("\t Erro: formato inválido de mensagem!");

        switch (operation) {
            case "compra":
                processBuyOrder(asset, broker, amount, value, channel);
                break;
            case "venda":
                processSellOrder(asset, broker, amount, value, channel);
                break;
        }
    }

    private static void processBuyOrder(String asset, String broker, String amount, String value, Channel channel) throws IOException {
        synchronized (orderBookLock) {
            Order buyOrder = new BuyOrder("compra", asset, broker, Integer.parseInt(amount), Double.parseDouble(value));
            List<Order> matchingOrders = orderBook.searchOrder(asset, Integer.parseInt(amount), Double.parseDouble(value));

            if (matchingOrders.isEmpty()) {
                orderBook.addOrder(buyOrder);
                notifyBrokers(new Notification("compra", asset, amount, value, broker), channel);
            } else if (matchingOrders.get(0).getOperation().equals("venda")) {
                Transaction transaction = new Transaction(buyOrder.getBroker(), matchingOrders.get(0).getBroker(), buyOrder);
                transactionBook.register(transaction);
                orderBook.removeOrder(matchingOrders.get(0));
            }
        }
    }

    private static void processSellOrder(String asset, String broker, String amount, String value, Channel channel) throws IOException {
        synchronized (orderBookLock) {
            Order sellOrder = new SellOrder("venda", asset, broker, Integer.parseInt(amount), Double.parseDouble(value));
            List<Order> matchingOrders = orderBook.searchOrder(asset, Integer.parseInt(amount), Double.parseDouble(value));

            if (matchingOrders.isEmpty()) {
                orderBook.addOrder(sellOrder);
                notifyBrokers(new Notification("venda", asset, amount, value, broker), channel);
            } else if (matchingOrders.get(0).getOperation().equals("compra")) {
                Transaction transaction = new Transaction(matchingOrders.get(0).getBroker(), sellOrder.getBroker(), sellOrder);
                transactionBook.register(transaction);
                orderBook.removeOrder(matchingOrders.get(0));
            }
        }
    }

    private static void notifyBrokers(Notification notification, Channel channel) throws IOException {
        String message = notification.buildMessage();
        channel.basicPublish("trading_exchange", notification.getBroker() + "." + notification.getAsset(), null, message.getBytes());
    }

    private static String[] splitTask(String task) {
        String[] splitted = new String[3];

        String[] firstHalf = task.split("\\.", 2);
        if (firstHalf.length != 2) return splitted;

        splitted[0] = firstHalf[0];
        String remaining = firstHalf[1];

        String[] remainingParts = remaining.split("<", 2);
        if (remainingParts.length != 2) return splitted;

        splitted[1] = remainingParts[0];
        splitted[2] = remainingParts[1].substring(0, remainingParts[1].length() - 1); // Remove '>' from `parameters`

        return splitted;
    }
}