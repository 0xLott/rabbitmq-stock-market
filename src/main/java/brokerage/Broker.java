package brokerage;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import io.github.cdimascio.dotenv.Dotenv;
import model.RabbitMQConnection;

import java.io.IOException;
import java.net.CacheRequest;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class Broker {
    private final static String EXCHANGE_NAME = "trading_exchange";
    private final static String QUEUE_NAME = "BROKER";

    public static void main(String[] argv) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("AMQP_URL");

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        /* PRODUCER THREAD
         * Sends buy and sell orders to the exchange so that the stock market can receive them through the
         * "BOLSADEVALORES" queue. Messages follow the format `operation.asset<amount;value;brokerId>`.
         */
        executorService.submit(() -> {
            try (Connection connection = RabbitMQConnection.createConnection(url);
                 Channel channel = RabbitMQConnection.createChannel(connection)) {

                String message1 = "compra.ABEV3<100;10,10;BKR1>";
                String message2 = "venda.PETR4<140;04,10;BKR1>";

                channel.basicPublish(EXCHANGE_NAME, "compra.ABEV3", null, message1.getBytes());
                channel.basicPublish(EXCHANGE_NAME, "venda.PETR4", null, message2.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        /* CONSUMER THREAD
         * Through the "BROKER" queue, recieves notifications from the stock market whenever there is a new buy or
         * sell order in one of the topics that the broker subscribes to.
         * TODO Implement subscrition system
         */
        executorService.submit(() -> {
            try (Connection connection = RabbitMQConnection.createConnection(url);
                 Channel channel = RabbitMQConnection.createChannel(connection)) {

                // Declare BROKER queue and bind to broker's identifier
                String brokerQueue = channel.queueDeclare(QUEUE_NAME, true, false, false, null).getQueue();
                channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "BRK1.*");

                // Handle message
                DeliverCallback deliverCallback = NotificationMessageHandler.createDeliverCallback(channel);

                // Acknoledge message and remove from queue
                channel.basicConsume(brokerQueue, true, deliverCallback, consumerTag -> {});

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        executorService.shutdown();
    }
}