package xyz.crearts.iot.registry.middlewate.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import xyz.crearts.iot.registry.service.DeviceManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.util.concurrent.AtomicDouble;
import xyz.crearts.iot.registry.service.MetricCollector;

import static org.springframework.amqp.rabbit.annotation.Exchange.TRUE;

@Component
@Slf4j
public class DeviceEventsListener {
    private final String PREFIX_MQTT_SUBSCRIPTION = "mqtt-subscription-";
    private final DeviceManager deviceManager;
    private final MetricCollector metricCollector;
    private final Consumer sysConsumer;

    private final ObjectMapper objectMapper = new ObjectMapper();


    public DeviceEventsListener(DeviceManager deviceManager, MetricCollector metricCollector, RabbitProperties properties) throws IOException, TimeoutException {
        this.deviceManager = deviceManager;
        this.metricCollector = metricCollector;

        var factory = new ConnectionFactory();
        factory.setUsername(properties.getUsername());
        factory.setPassword(properties.getPassword());
        factory.setVirtualHost("/");

        var addrs = properties.getAddresses().stream().map(Address::new).toList();
        try (var conn = factory.newConnection(addrs)) {
            var ch = conn.createChannel();
            String q = ch.queueDeclare().getQueue();
            ch.queueBind(q, "amq.rabbitmq.event", "connection.*");
            sysConsumer = new DefaultConsumer(ch) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String key = envelope.getRoutingKey();
                    Map<String, Object> headers = properties.getHeaders();

                    String connName = ObjectUtils.nullSafeToString(headers.get("name"));
                    if (!connName.endsWith(":1883") && !connName.equals(":8883")) {
                        return;
                    }

                    String sessionId = ObjectUtils.nullSafeToString(headers.get("name"));
                    long timestamp = (long) headers.get("timestamp");
                    String productName = ObjectUtils.nullSafeToString(headers.get("vhost"));
                    String clientId = "unknown";
                    try {
                        var clientProps = headers.get("client_properties");
                        if (null != clientProps) {
                            clientId = ((java.util.ArrayList<String>) headers.get("client_properties")).get(1).split("\"")[1];
                        }
                    } catch (Throwable ignored) {
                    }

                    if (key.equals("connection.created")) {
                        log.info("device online: {}, {}", productName, clientId);
                        deviceManager.deviceOnline(productName, clientId, sessionId);
                    } else if (key.equals("connection.closed")) {
                        log.info("device offline: {}, {}", productName, clientId);
                        deviceManager.deviceOffline(sessionId, timestamp);
                    }
                }
            };
            ch.basicConsume(q, true, sysConsumer);
        }
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "mqtt-publishing.device.telemetry", durable = TRUE),
                    exchange = @Exchange(value = "amq.topic", ignoreDeclarationExceptions = TRUE, type = ExchangeTypes.TOPIC),
                    key = ".*.*.sys.telemetry"
            )
    )
    void deviceTelemetry(@Header("amqp_receivedRoutingKey") String key, @Headers Map<String, Object> headers, @Payload String payload) throws JsonProcessingException {
        log.info(key);
        headers.forEach((k, v) -> {
            log.info("\t{}: {}", k, v);
        });
        log.info("payload: {}", payload);

        var keys = key.split("\\.");
        if (keys.length >= 3) {
            var metrics = objectMapper.readValue(payload, new TypeReference<HashMap<String, Object>>() {});
            metrics.forEach((k, v) -> {

                if (v instanceof Number) {
                    metricCollector.updateGauge(k, keys[1], keys[2], ((Number) v).doubleValue());
                }
            });
        }
    }
}
