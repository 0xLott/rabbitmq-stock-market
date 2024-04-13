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
                System.out.println(" [x] Received '" + message + "' on topic '" + routingKey + "'");
                handle(message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println(" [x] Done" + "\n");
            }
        };
    }

    private static void handle(String task) throws InterruptedException {
        // handle here, the messages should have information on wheter is a new buy or sell order, its value, etc
    }
}
