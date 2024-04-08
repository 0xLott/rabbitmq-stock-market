package exchange;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import io.github.cdimascio.dotenv.Dotenv;
import model.MessageHandler;
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
            String queueName = channel.queueDeclare().getQueue();

            if (argv.length < 1) {
                System.err.println("Parâmetros não encontrados");
                System.exit(1);
            }

            // Bind the queue to each stock topic
            for (String bindingKey : argv) {
                channel.queueBind(queueName, EXCHANGE_NAME, "compra." + bindingKey);
                channel.queueBind(queueName, EXCHANGE_NAME, "venda." + bindingKey);
            }

            DeliverCallback deliverCallback = MessageHandler.createDeliverCallback(channel);

            boolean autoAck = false;
            channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> {});

            // Wait for new messages to arrive
            System.out.println(" [*] Waiting for messages related to stocks: " + Arrays.toString(argv));
            while (true) {
                Thread.sleep(1000);
            }
        }
    }
}