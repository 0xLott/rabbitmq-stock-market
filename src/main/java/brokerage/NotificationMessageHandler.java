package brokerage;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

public class NotificationMessageHandler {
    public static DeliverCallback createDeliverCallback(Channel channel) {

        // Parameters `consumerTag` and `delivery` are defined by the DeliverCallback functional interface
        return (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            String routingKey = delivery.getEnvelope().getRoutingKey();

            try {
                System.out.println("\n [x] Received '" + message + "' on topic '" + routingKey + "'");
                handle(message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println(" [x] Done" + "\n");
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

        System.out.println(
                " ⚠ Nova ordem de " + operation + ":\n" +
                        " " + amount + " ações " + asset + " no valor de " +
                        "R$" + value + " por broker " + broker + "\n"
        );

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