package xyz.crearts.iot.registry.middlewate.queue;

import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.rabbitmq.client.*;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author ivan.kishchenko
 * email ivan.kishchenko@lazada.com
 */
@Configuration
public class RabbitMQConfig {
    private final String PREFIX_MQTT_SUBSCRIPTION = "mqtt-subscription-";

    private boolean filterEvents(AMQP.BasicProperties properties) {
        Map<String, Object> headers = properties.getHeaders();

        String protocol = ObjectUtils.nullSafeToString(headers.get("protocol"));
        if (!protocol.contains("'MQTT'")) {
            return false;
        }

        return true;
    }

    @Bean(destroyMethod = "close")
    Connection systemRabbitMQConnection(RabbitProperties properties) throws IOException, TimeoutException {
        var factory = new ConnectionFactory();
        factory.setUsername(properties.getUsername());
        factory.setPassword(properties.getPassword());
        factory.setVirtualHost("/");

        var addrs = properties.getAddresses().stream().map(Address::new).toList();
        return factory.newConnection(addrs);
    }

    @Bean
    Consumer deviceConnectionEventConsumer(Connection connection, DeviceEventsListener listener) throws IOException {
        var ch = connection.createChannel();
        String q = ch.queueDeclare("events.connection", false, true, true, null).getQueue();
        ch.queueBind(q, "amq.rabbitmq.event", "connection.*");
        var consumer = new DefaultConsumer(ch) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                if (!filterEvents(properties)) {
                    return;
                }
                listener.deviceConnectionEvent(envelope, properties);

            }
        };
        ch.basicConsume(q, true, consumer);

        return consumer;
    }

    @Bean
    Consumer deviceQueueEventConsumer(Connection connection, DeviceEventsListener listener) throws IOException {
        var ch = connection.createChannel();
        String q = ch.queueDeclare("events.queue", false, true, true, null).getQueue();
        ch.queueBind(q, "amq.rabbitmq.event", "queue.*");
        var consumer = new DefaultConsumer(ch) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String type = ObjectUtils.nullSafeToString(properties.getHeaders().get("type"));
                if (!type.startsWith("rabbit_mqtt")) {
                    return;
                }

                listener.deviceQueueEvent(envelope, properties);

            }
        };
        ch.basicConsume(q, true, consumer);

        return consumer;
    }

    @Bean
    Consumer deviceBindEventConsumer(Connection connection, DeviceEventsListener listener) throws IOException {
        var ch = connection.createChannel();
        String q = ch.queueDeclare("events.binding", false, true, true, null).getQueue();
        ch.queueBind(q, "amq.rabbitmq.event", "binding.*");
        var consumer = new DefaultConsumer(ch) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                Map<String, Object> headers = properties.getHeaders();
                var dest = headers.getOrDefault("destination_name", "").toString();
                if (dest.startsWith(PREFIX_MQTT_SUBSCRIPTION)) {
                    listener.deviceBindingEvent(envelope, properties);
                }

            }
        };
        ch.basicConsume(q, true, consumer);

        return consumer;
    }
}
