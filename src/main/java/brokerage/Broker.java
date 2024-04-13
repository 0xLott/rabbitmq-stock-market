package brokerage;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import io.github.cdimascio.dotenv.Dotenv;
import model.OrderMessageHandler;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class Broker {
    private final static String EXCHANGE_NAME = "trading_exchange";
    private final static String QUEUE_NAME = "BROKER";

    public static void main(String[] argv) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("AMQP_URL");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(url);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        /*
            PRODUCER
         */
        executorService.submit(() -> {
            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                String message1 = "compra.ABEV3<100;10,10;BKR1>";
                String message2 = "venda.PETR4<140;04,10;BKR1>";

                channel.basicPublish(EXCHANGE_NAME, "compra.ABEV3", null, message1.getBytes());
                channel.basicPublish(EXCHANGE_NAME, "venda.PETR4", null, message2.getBytes());
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        });

        /*
            CONSUMER
         */
        executorService.submit(() -> {
            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                String queueName = channel.queueDeclare(QUEUE_NAME, true, false, false, null).getQueue();
                channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "BRK1.*");

                // Handle message
                DeliverCallback deliverCallback = NotificationMessageHandler.createDeliverCallback(channel);

                // Acknoledge message and remove from queue
                boolean autoAck = false;
                channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> {});

            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        });
    }
}