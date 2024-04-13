package connection;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

public class RabbitMQConnection {
    private static final String EXCHANGE_NAME = "trading_exchange";

    public static Connection createConnection(String url) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(url);
        return factory.newConnection();
    }

    public static Channel createChannel(Connection connection) throws IOException {
        return connection.createChannel();
    }

    public static void declareExchange(Channel channel) throws IOException {
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
    }
}