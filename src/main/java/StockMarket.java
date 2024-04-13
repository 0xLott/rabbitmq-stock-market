import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import io.github.cdimascio.dotenv.Dotenv;
import model.OrderMessageHandler;
import model.RabbitMQConnection;

import java.util.Arrays;

public class StockMarket {
    private final static String EXCHANGE_NAME = "trading_exchange";

    public static void main(String[] argv) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("AMQP_URL");

        try (Connection connection = RabbitMQConnection.createConnection(url);
             Channel channel = RabbitMQConnection.createChannel(connection)) {

            RabbitMQConnection.declareExchange(channel);
            String stockMarketQueue = channel.queueDeclare().getQueue();

            if (argv.length < 1) {
                System.err.println("Parâmetros não encontrados! Adicione-os nos argumentos de execução");
                System.exit(1);
            }

            // Bind the queue to each stock topic
            for (String bindingKey : argv) {
                channel.queueBind(stockMarketQueue, EXCHANGE_NAME, "compra." + bindingKey);
                channel.queueBind(stockMarketQueue, EXCHANGE_NAME, "venda." + bindingKey);
            }

            // Handle message
            DeliverCallback deliverCallback = OrderMessageHandler.createDeliverCallback(channel);

            // Acknoledge message and remove from queue
            boolean autoAck = false;
            channel.basicConsume(stockMarketQueue, autoAck, deliverCallback, consumerTag -> {});

            // NOTIFICATION TEST SEND
            // TODO Delete this
            for (int i = 0; i < 10; i++) {
                String notification = "compra.VALE5<50;10,10;BKR1>";
                channel.basicPublish(EXCHANGE_NAME, "BRK1.teste", null, notification.getBytes());
                System.out.println("Notification send!");
            }

            // Wait for new messages to arrive
            System.out.println(" [*] Waiting for messages related to stocks: " + Arrays.toString(argv));
            while (true) {
                Thread.sleep(1000);
            }
        }
    }
}