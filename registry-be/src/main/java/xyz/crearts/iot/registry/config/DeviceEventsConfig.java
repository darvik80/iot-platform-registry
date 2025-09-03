package xyz.crearts.iot.registry.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for device events processing
 */
@Component
@ConfigurationProperties(prefix = "device.events")
public class DeviceEventsConfig {

    // MQTT port constants
    public static final String MQTT_PORT_1883 = ":1883";
    public static final String MQTT_SECURE_PORT_8883 = ":8883";

    // RabbitMQ constants
    public static final String RABBITMQ_EVENT_EXCHANGE = "amq.rabbitmq.event";
    public static final String CONNECTION_ROUTING_PATTERN = "connection.*";
    public static final String MQTT_TOPIC_EXCHANGE = "amq.topic";
    public static final String TELEMETRY_QUEUE = "mqtt-publishing.device.telemetry";
    public static final String TELEMETRY_ROUTING_KEY = ".*.*.sys.telemetry";

    // Connection event constants
    public static final String CONNECTION_CREATED = "connection.created";
    public static final String CONNECTION_CLOSED = "connection.closed";

    // Header constants
    public static final String HEADER_NAME = "name";
    public static final String HEADER_TIMESTAMP = "timestamp";
    public static final String HEADER_VHOST = "vhost";
    public static final String HEADER_CLIENT_PROPERTIES = "client_properties";

    // Default constants
    public static final String DEFAULT_CLIENT_ID = "unknown";
    public static final String DEFAULT_VIRTUAL_HOST = "/";

    // Minimum telemetry key parts length
    public static final int MIN_TELEMETRY_KEY_PARTS = 3;
}
