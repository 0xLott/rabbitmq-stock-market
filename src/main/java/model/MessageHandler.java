package model;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import exchange.BuyOrder;
import exchange.Order;
import exchange.OrderBook;
import exchange.SellOrder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageHandler {
    static OrderBook orderbook = new OrderBook(new ArrayList<>());
    static ExecutorService executor = Executors.newFixedThreadPool(2);

    /*
     * ExecutorService allows this method to return right after submitting the task, enabling it to handle more
     * messages concurrently.
     */
    public static DeliverCallback createDeliverCallback(Channel channel) {
        return (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            String routingKey = delivery.getEnvelope().getRoutingKey();

            try {
                System.out.println(" [x] Received '" + message + "' on topic '" + routingKey + "'");
                executor.execute(() -> handle(message));
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println(" [x] Done");
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
    }

    private static void handle(String task) {
        String[] parts = splitTask(task);
        String operation = parts[0];
        String asset = parts[1];

        String[] parameters = parts[2].split(";");
        String amount = parameters[0];
        String value = parameters[1];
        String broker = parameters[2];
        value = value.replace(",", ".");

        if (operation == null || asset == null || parameters.length != 3) {
            System.out.println("\t Erro: formato inválido de mensagem!");
            return;
        }

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
        executor.execute(() -> {
            Order buyOrder = new BuyOrder(asset, broker, Integer.parseInt(amount), Double.parseDouble(value));
            orderbook.addOrder(buyOrder);
            System.out.println(buyOrder.toString());
        });
    }

    private static void processSellOrder(String asset, String broker, String amount, String value) {
        executor.execute(() -> {
            Order sellOrder = new SellOrder(asset, broker, Integer.parseInt(amount), Double.parseDouble(value));
            orderbook.addOrder(sellOrder);
            System.out.println(sellOrder.toString());
        });
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
        parts[2] = remainingParts[1].substring(0, remainingParts[1].length() - 1); // Remove '>' from parameters

        return parts;
    }
}
