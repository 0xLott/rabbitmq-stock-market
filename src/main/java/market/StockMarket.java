package market;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import io.github.cdimascio.dotenv.Dotenv;
import connection.RabbitMQConnection;

import java.util.Arrays;

public class StockMarket {
    private final static String EXCHANGE_NAME = "trading_exchange";
    private final static String QUEUE_NAME = "BROKER";

    public static void main(String[] argv) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("AMQP_URL");

        try (Connection connection = RabbitMQConnection.createConnection(url);
             Channel channel = RabbitMQConnection.createChannel(connection)) {

            if (argv.length < 1) {
                System.err.println("Parâmetros não encontrados! Adicione-os nos argumentos de execução");
                System.exit(1);
            }

            // Declare exchange and BROKER queue
            RabbitMQConnection.declareExchange(channel);
            String brokerQueue = channel.queueDeclare(QUEUE_NAME, true, false, false, null).getQueue();

            // Bind the queue to each stock topic
            for (String bindingKey : argv) {
                channel.queueBind(brokerQueue, EXCHANGE_NAME, bindingKey);
            }

            // Handle message
            DeliverCallback deliverCallback = OrderMessageHandler.createDeliverCallback(channel);

            // Acknoledge message and remove from queue
            boolean autoAck = false;
            channel.basicConsume(brokerQueue, autoAck, deliverCallback, consumerTag -> {
            });

            // SEND NOTIFICATION TEST
//            for (int i = 0; i < 3; i++) {
//                String notification = "compra.VALE5<50;10,10;BKR1>";
//                channel.basicPublish(EXCHANGE_NAME, "BRK1.teste", null, notification.getBytes());
//                System.out.println("Notification send!");
//            }

            System.out.println("Transaction book:");

            // Wait for new messages to arrive
            System.out.println(" [*] Waiting for messages related to stocks: " + Arrays.toString(argv));
            while (true) {
                Thread.sleep(1000);
            }
        }
    }
}