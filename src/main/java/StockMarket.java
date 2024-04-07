import com.rabbitmq.client.*;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Arrays;

public class StockMarket {
    private final static String EXCHANGE_NAME = "trading_exchange";

    public static void main(String[] argv) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("AMQP_URL");

        // Connect to CloudAMQP server
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(url);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");
            String queueName = channel.queueDeclare().getQueue();

            if (argv.length < 1) {
                System.err.println("Parâmetros não encontrados");
                System.exit(1);
            }

            // Bind the queue to each stock topic
            for (String bindingKey : argv) {
                channel.queueBind(queueName, EXCHANGE_NAME, "stock." + bindingKey);
            }

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                String routingKey = delivery.getEnvelope().getRoutingKey();

                try {
                    System.out.println(" [x] Received '" + message + "' on topic '" + routingKey + "'");
                    doWork(message);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    System.out.println(" [x] Done");
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };

            boolean autoAck = false;
            channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> {});

            // Aguarda chegada de mensagens
            System.out.println(" [*] Waiting for messages related to stocks: " + Arrays.toString(argv));
            while (true) {
                Thread.sleep(1000);
            }
        }
    }

    /**
     * Simula tempo de execução de uma tarefa
     */
    private static void doWork(String task) throws InterruptedException {
        for (char ch : task.toCharArray()) {
            if (ch == '.') Thread.sleep(3000);
        }
    }
}
