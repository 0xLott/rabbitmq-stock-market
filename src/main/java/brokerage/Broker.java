package brokerage;

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

            String message = "compra.ABEV3<100;10,10;BKR1>";

            channel.basicPublish(EXCHANGE_NAME, "compra.ABEV3", null, message.getBytes());
            channel.basicPublish(EXCHANGE_NAME, "venda.PETR4", null, message.getBytes());
        }
    }
}