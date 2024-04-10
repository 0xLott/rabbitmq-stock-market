package model;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import exchange.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MessageHandler {
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
                handle(message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println(" [x] Done");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
    }

    private static void handle(String task) throws InterruptedException {
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
                processBuyOrder(asset, broker, amount, value);
                break;
            case "venda":
                processSellOrder(asset, broker, amount, value);
                break;
        }
    }

    private static void processBuyOrder(String asset, String broker, String amount, String value) {
        synchronized (orderBookLock) {
            Order buyOrder = new BuyOrder(asset, broker, Integer.parseInt(amount), Double.parseDouble(value));
            List<Order> matchingOrders = orderBook.searchOrder(asset, Integer.parseInt(amount), Double.parseDouble(value));

            if (matchingOrders.isEmpty()) {
                orderBook.addOrder(buyOrder);
            } else {
                Transaction transaction = new Transaction(buyOrder.getBroker(), matchingOrders.get(0).getBroker(), buyOrder);
                transactionBook.register(transaction);
                orderBook.removeOrder(matchingOrders.get(0));
            }
        }
    }

    private static void processSellOrder(String asset, String broker, String amount, String value) {
        synchronized (orderBookLock) {
            Order sellOrder = new SellOrder(asset, broker, Integer.parseInt(amount), Double.parseDouble(value));
            List<Order> matchingOrders = orderBook.searchOrder(asset, Integer.parseInt(amount), Double.parseDouble(value));

            if (matchingOrders.isEmpty()) {
                orderBook.addOrder(sellOrder);
            } else {
                Transaction transaction = new Transaction(matchingOrders.get(0).getBroker(), sellOrder.getBroker(), sellOrder);
                transactionBook.register(transaction);
                orderBook.removeOrder(matchingOrders.get(0));
            }
        }
    }

    private static String[] splitTask(String task) {
        String[] parts = new String[3];

        String[] firstSplit = task.split("\\.", 2);
        if (firstSplit.length != 2) return parts;

        parts[0] = firstSplit[0];
        String remaining = firstSplit[1];

        String[] remainingParts = remaining.split("<", 2);
        if (remainingParts.length != 2) return parts;

        parts[1] = remainingParts[0];
        parts[2] = remainingParts[1].substring(0, remainingParts[1].length() - 1); // Remove '>' from `parameters`

        return parts;
    }
}