package brokerage;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import io.github.cdimascio.dotenv.Dotenv;
import connection.RabbitMQConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Broker {
    private final static String EXCHANGE_NAME = "trading_exchange";
    private final static String QUEUE_NAME = "BOLSADEVALORES";
    private final List<String> subscribedAssets = new ArrayList<>();

    public static void main(String[] argv) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("AMQP_URL");

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Connection connection = RabbitMQConnection.createConnection(url);
        Channel channel = RabbitMQConnection.createChannel(connection);

        /* PRODUCER THREAD
         * Sends buy and sell orders to the exchange so that the stock market.market can receive them through the
         * "BROKER" queue. Messages follow the format `operation.asset<amount;value;brokerId>`.
         */
        executorService.submit(() -> {
            try {

                String message1 = "compra.ABEV3<100;10,10;BKR1>";
                String message2 = "venda.PETR4<140;04,10;BKR1>";

                channel.basicPublish(EXCHANGE_NAME, "ABEV3", null, message1.getBytes());
                channel.basicPublish(EXCHANGE_NAME, "PETR4", null, message2.getBytes());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        /* CONSUMER THREAD
         * Through the "BOLSADEVALORES" queue, recieves notifications from the stock market.market whenever there is a new buy or
         * sell order in one of the topics that the broker subscribes to.
         * TODO Implement subscrition system
         */
        executorService.submit(() -> {
            try {

                // Declare BOLSADEVALORES queue and bind to broker's identifier
                String stockMarketQueue = channel.queueDeclare(QUEUE_NAME, true, false, false, null).getQueue();

                // Subscribe to assets
                Broker broker = new Broker();
                broker.subscribe("PETR4");
                broker.subscribe("ABEV3");

                // Bind queue to subscribed assets
                for (String asset : broker.getSubscribedAssets()) {
                    channel.queueBind(stockMarketQueue, EXCHANGE_NAME, "BKR1." + asset);
                }

                // Handle message
                DeliverCallback deliverCallback = NotificationMessageHandler.createDeliverCallback(channel);

                // Acknoledge message and remove from queue
                channel.basicConsume(stockMarketQueue, true, deliverCallback, consumerTag -> {
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void subscribe(String asset) {
        subscribedAssets.add(asset);
    }

    private boolean isSubscribed(String asset) {
        return subscribedAssets.contains(asset);
    }

    public List<String> getSubscribedAssets() {
        return subscribedAssets;
    }
}