import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.cdimascio.dotenv.Dotenv;

public class Broker {
    private final static String EXCHANGE_NAME = "trading_exchange";

    public static void main(String[] argv) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("AMQP_URL");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(url);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, "topic");

            String message = "Ordem de compra: 100 ações";

            // Use routing key like "stock.ABEV3.PETR4"
            channel.basicPublish(EXCHANGE_NAME, "stock.ABEV3", null, message.getBytes());
            channel.basicPublish(EXCHANGE_NAME, "stock.PETR4", null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}