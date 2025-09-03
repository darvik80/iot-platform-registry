package xyz.crearts.iot.registry.middlewate.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import xyz.crearts.iot.registry.config.DeviceEventsConfig;
import xyz.crearts.iot.registry.service.DeviceManager;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.springframework.amqp.rabbit.annotation.Exchange.TRUE;

/**
 * Device events listener for handling MQTT connections/disconnections
 * and telemetry data through RabbitMQ
 */
@Component
@Slf4j
public class DeviceEventsListener implements DisposableBean {

    private final DeviceManager deviceManager;
    private final RabbitProperties rabbitProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Connection connection;
    private Channel channel;
    private Consumer sysConsumer;
    private String consumerTag;

    public DeviceEventsListener(DeviceManager deviceManager,
                               RabbitProperties rabbitProperties) {
        this.deviceManager = deviceManager;
        this.rabbitProperties = rabbitProperties;
    }

    @PostConstruct
    public void initialize() {
        try {
            setupSystemEventListener();
            log.info("DeviceEventsListener successfully initialized");
        } catch (Exception e) {
            log.error("Error initializing DeviceEventsListener", e);
            throw new RuntimeException("Failed to initialize DeviceEventsListener", e);
        }
    }

    private void setupSystemEventListener() throws IOException, TimeoutException {
        ConnectionFactory factory = createConnectionFactory();
        List<Address> addresses = rabbitProperties.getAddresses().stream()
            .map(Address::new)
            .toList();

        connection = factory.newConnection(addresses);
        channel = connection.createChannel();

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, DeviceEventsConfig.RABBITMQ_EVENT_EXCHANGE, 
                         DeviceEventsConfig.CONNECTION_ROUTING_PATTERN);

        sysConsumer = createSystemEventConsumer();
        consumerTag = channel.basicConsume(queueName, true, sysConsumer);

        log.info("System event listener configured for queue: {}, consumerTag: {}", queueName, consumerTag);
    }

    private ConnectionFactory createConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(rabbitProperties.getUsername());
        factory.setPassword(rabbitProperties.getPassword());
        factory.setVirtualHost(DeviceEventsConfig.DEFAULT_VIRTUAL_HOST);
        return factory;
    }

    private Consumer createSystemEventConsumer() {
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, 
                                     AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    handleSystemEvent(envelope.getRoutingKey(), properties.getHeaders());
                } catch (Exception e) {
                    log.error("Error processing system event: {}", envelope.getRoutingKey(), e);
                }
            }
        };
    }

    private void handleSystemEvent(String routingKey, Map<String, Object> headers) {
        if (headers == null) {
            log.warn("Received empty headers for event: {}", routingKey);
            return;
        }

        String connectionName = extractConnectionName(headers);
        if (!isMqttConnection(connectionName)) {
            return;
        }

        String sessionId = extractSessionId(headers);
        String productName = extractProductName(headers);
        String clientId = extractClientId(headers);

        if (DeviceEventsConfig.CONNECTION_CREATED.equals(routingKey)) {
            handleDeviceOnline(productName, clientId, sessionId);
        } else if (DeviceEventsConfig.CONNECTION_CLOSED.equals(routingKey)) {
            long timestamp = extractTimestamp(headers);
            handleDeviceOffline(sessionId, timestamp);
        }
    }

    private String extractConnectionName(Map<String, Object> headers) {
        return ObjectUtils.nullSafeToString(headers.get(DeviceEventsConfig.HEADER_NAME));
    }

    private boolean isMqttConnection(String connectionName) {
        return StringUtils.hasText(connectionName) && 
               (connectionName.endsWith(DeviceEventsConfig.MQTT_PORT_1883) || 
                connectionName.endsWith(DeviceEventsConfig.MQTT_SECURE_PORT_8883));
    }

    private String extractSessionId(Map<String, Object> headers) {
        return ObjectUtils.nullSafeToString(headers.get(DeviceEventsConfig.HEADER_NAME));
    }

    private String extractProductName(Map<String, Object> headers) {
        return ObjectUtils.nullSafeToString(headers.get(DeviceEventsConfig.HEADER_VHOST));
    }

    private String extractClientId(Map<String, Object> headers) {
        try {
            Object clientPropsObj = headers.get(DeviceEventsConfig.HEADER_CLIENT_PROPERTIES);
            if (clientPropsObj instanceof List<?> clientProps && !clientProps.isEmpty()) {
                String propsString = clientProps.get(1).toString();
                if (StringUtils.hasText(propsString) && propsString.contains("\"")) {
                    String[] parts = propsString.split("\"");
                    if (parts.length > 1) {
                        return parts[1];
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract client_id from headers: {}", e.getMessage());
        }
        return DeviceEventsConfig.DEFAULT_CLIENT_ID;
    }

    private long extractTimestamp(Map<String, Object> headers) {
        Object timestampObj = headers.get(DeviceEventsConfig.HEADER_TIMESTAMP);
        if (timestampObj instanceof Long) {
            return (Long) timestampObj;
        } else if (timestampObj instanceof Number) {
            return ((Number) timestampObj).longValue();
        }
        log.warn("Failed to extract timestamp, using current time");
        return System.currentTimeMillis();
    }

    private void handleDeviceOnline(String productName, String clientId, String sessionId) {
        try {
            log.info("Device connected: product={}, client={}, session={}", 
                    productName, clientId, sessionId);
            deviceManager.deviceOnline(productName, clientId, sessionId);
        } catch (Exception e) {
            log.error("Error handling device connection: product={}, client={}", 
                     productName, clientId, e);
        }
    }

    private void handleDeviceOffline(String sessionId, long timestamp) {
        try {
            log.info("Device disconnected: session={}, timestamp={}", sessionId, timestamp);
            deviceManager.deviceOffline(sessionId, timestamp);
        } catch (Exception e) {
            log.error("Error handling device disconnection: session={}", sessionId, e);
        }
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = DeviceEventsConfig.TELEMETRY_QUEUE, durable = TRUE),
                    exchange = @Exchange(value = DeviceEventsConfig.MQTT_TOPIC_EXCHANGE, 
                                       ignoreDeclarationExceptions = TRUE, 
                                       type = ExchangeTypes.TOPIC),
                    key = DeviceEventsConfig.TELEMETRY_ROUTING_KEY
            )
    )
    public void handleDeviceTelemetry(@Header("amqp_receivedRoutingKey") String routingKey, 
                                    @Headers Map<String, Object> headers, 
                                    @Payload String payload) {
        try {
            log.debug("Получена телеметрия: ключ={}", routingKey);

            if (log.isTraceEnabled()) {
                headers.forEach((k, v) -> log.trace("Заголовок: {}={}", k, v));
                log.trace("Полезная нагрузка: {}", payload);
            }

            processTelemetryData(routingKey, payload);
        } catch (Exception e) {
            log.error("Ошибка при обработке телеметрии: ключ={}, полезная нагрузка={}", 
                     routingKey, payload, e);
        }
    }

    private void processTelemetryData(String routingKey, String payload) throws JsonProcessingException {
        String[] keyParts = routingKey.split("\\.");
        if (keyParts.length < DeviceEventsConfig.MIN_TELEMETRY_KEY_PARTS) {
            log.warn("Некорректный формат ключа телеметрии: {}, ожидается минимум {} частей", 
                    routingKey, DeviceEventsConfig.MIN_TELEMETRY_KEY_PARTS);
            return;
        }

        if (!StringUtils.hasText(payload)) {
            log.warn("Получена пустая полезная нагрузка для ключа: {}", routingKey);
            return;
        }

        Map<String, Object> metrics = parseMetrics(payload);
        String deviceType = keyParts[1];
        String deviceId = keyParts[2];

        metrics.forEach((metricName, metricValue) -> {
            try {
                if (metricValue instanceof Number) {
                    double value = ((Number) metricValue).doubleValue();
                    log.debug("Обновлена метрика: {}={}. устройство={}.{}",
                             metricName, value, deviceType, deviceId);
                } else {
                    log.debug("Пропущена не числовая метрика: {}={}", metricName, metricValue);
                }
            } catch (Exception e) {
                log.error("Ошибка при обновлении метрики: {}={}", metricName, metricValue, e);
            }
        });
    }

    private Map<String, Object> parseMetrics(String payload) throws JsonProcessingException {
        try {
            return objectMapper.readValue(payload, new TypeReference<HashMap<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Ошибка парсинга JSON полезной нагрузки: {}", payload, e);
            throw e;
        }
    }

    @PreDestroy
    @Override
    public void destroy() {
        log.info("Закрытие DeviceEventsListener...");

        try {
            if (consumerTag != null && channel != null && channel.isOpen()) {
                channel.basicCancel(consumerTag);
                log.debug("Потребитель отменен: {}", consumerTag);
            }
        } catch (Exception e) {
            log.warn("Ошибка при отмене потребителя: {}", consumerTag, e);
        }

        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
                log.debug("Канал закрыт");
            }
        } catch (Exception e) {
            log.warn("Ошибка при закрытии канала", e);
        }

        try {
            if (connection != null && connection.isOpen()) {
                connection.close();
                log.debug("Соединение закрыто");
            }
        } catch (Exception e) {
            log.warn("Ошибка при закрытии соединения", e);
        }

        log.info("DeviceEventsListener успешно закрыт");
    }
}
