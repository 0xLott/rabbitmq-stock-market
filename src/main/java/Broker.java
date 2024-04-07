import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.github.cdimascio.dotenv.Dotenv;

public class Broker {
    private final static String QUEUE_NAME = "BROKER";

    public static void main(String[] argv) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("AMQP_URL");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(url);

        try (Connection connection = factory.newConnection();
            Channel BOLSADEVALORES = connection.createChannel()) {
            BOLSADEVALORES.queueDeclare(QUEUE_NAME, true, false, false, null);

            String message = "Hello, RabbitMQ!";

            BOLSADEVALORES.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
