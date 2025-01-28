package xyz.crearts.iot.registry.middlewate.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import xyz.crearts.iot.registry.middlewate.event.DeviceConnectionEvent;
import xyz.crearts.iot.registry.middlewate.event.DeviceEventType;
import xyz.crearts.iot.registry.middlewate.event.DeviceSubscribeEvent;
import xyz.crearts.iot.registry.service.DeviceManager;
import xyz.crearts.iot.registry.service.MetricCollector;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.amqp.rabbit.annotation.Exchange.TRUE;

@Component
@Slf4j
@RequiredArgsConstructor
public class DeviceEventsListener {
    private final DeviceManager deviceManager;
    private final MetricCollector metricCollector;

    private final ObjectMapper objectMapper = new ObjectMapper();

    DeviceConnectionEvent parceDeviceConnectionMessage(Envelope envelope, AMQP.BasicProperties properties) {
        Map<String, Object> headers = properties.getHeaders();
        var clientProperties = headers.get(RabbitMQHeader.MQTT_CLIENT_PROPERTIES);

        if (!(clientProperties instanceof java.util.ArrayList<?>)) {
            return null;
        }

        var props = (java.util.ArrayList<?>)(clientProperties);
        var prop = props.stream().filter(p -> p.toString().startsWith("{client_id")).findFirst();
        if (prop.isPresent()) {
            var data = StringUtils.trimTrailingCharacter(
                    StringUtils.trimLeadingCharacter(prop.get().toString(), '{'), '}'

            ).split(",");

            if (data.length == 3) {
                var clientId = StringUtils.trimTrailingCharacter(
                        StringUtils.trimLeadingCharacter(data[2], '<'), '>'
                );

                clientId = StringUtils.trimTrailingCharacter(
                        StringUtils.trimLeadingCharacter(clientId, '"'), '"'
                );

                var deviceInfo = clientId.split("\\|");
                if (deviceInfo.length == 3) {
                    return DeviceConnectionEvent.builder()
                            .event(DeviceEventType.fromRoutingKey(envelope.getRoutingKey()))
                            .pid(ObjectUtils.nullSafeToString(headers.get(RabbitMQHeader.PID)))
                            .clientId(clientId)
                            .productName(deviceInfo[0])
                            .deviceName(deviceInfo[1])
                            .macAddress(deviceInfo[2])
                            .sessionId(ObjectUtils.nullSafeToString(headers.get(RabbitMQHeader.NAME)))
                            .timestamp((long) headers.get(RabbitMQHeader.TIMESTAMP))
                            .build();
                }
            }
        }

        return null;
    }

    DeviceSubscribeEvent parceDeviceSubscribeMessage(Envelope envelope, AMQP.BasicProperties properties) {
        Map<String, Object> headers = properties.getHeaders();
        var topic = headers.get(RabbitMQHeader.ROUTING_KEY).toString();
        var vhost = headers.get(RabbitMQHeader.VHOST).toString();
        var data = topic.split("\\.");
        if (data.length < 4) {
            return null;
        }

        if (!vhost.equals(data[1])) {
            return null;
        }

        int idx = 1;
        var productName = data[idx++];
        var deviceName = data.length == 4 ? "broadcast" : data[idx++];
        var module = data[idx++];
        var component = data[idx];

        return DeviceSubscribeEvent.builder()
                .event(DeviceEventType.fromRoutingKey(envelope.getRoutingKey()))
                .productName(productName)
                .deviceName(deviceName)
                .topic(topic)
                .module(module)
                .component(component)
                .timestamp((long)headers.get(RabbitMQHeader.TIMESTAMP))
                .build();

    }

    void deviceConnectionEvent(Envelope envelope, AMQP.BasicProperties properties) {
        Map<String, Object> headers = properties.getHeaders();

        String productName = ObjectUtils.nullSafeToString(headers.get(RabbitMQHeader.VHOST));
        var deviceInfo = parceDeviceConnectionMessage(envelope, properties);
        if (deviceInfo != null) {
            if (!productName.equals(deviceInfo.getProductName())) {
                return;
            }

            if (deviceInfo.getEvent() == DeviceEventType.DEVICE_CONNECTED) {
                log.info("device online: {}, {}", deviceInfo.getProductName(), deviceInfo.getDeviceName());
                deviceManager.deviceOnline(deviceInfo);
            } else if (deviceInfo.getEvent() == DeviceEventType.DEVICE_DISCONNECTED) {
                log.info("device offline: {}, {}", deviceInfo.getProductName(), deviceInfo.getDeviceName());
                deviceManager.deviceOffline(deviceInfo);
            }
        }
    }

    void deviceQueueEvent(Envelope envelope, AMQP.BasicProperties properties) {
        // TODO: how we could use it?
        var deviceInfo = parceDeviceConnectionMessage(envelope, properties);
    }

    void deviceBindingEvent(Envelope envelope, AMQP.BasicProperties properties) {
        var deviceInfo = parceDeviceSubscribeMessage(envelope, properties);
        if (deviceInfo.getEvent() == DeviceEventType.DEVICE_SUBSCRIBED) {
            log.info("device sub: {}:{}, {}", deviceInfo.getProductName(), deviceInfo.getDeviceName(), deviceInfo.getTopic());
            deviceManager.deviceSubscribe(deviceInfo);
        } else if (deviceInfo.getEvent() == DeviceEventType.DEVICE_UNSUBSCRIBED) {
            log.info("device un-sub: {}:{}, {}", deviceInfo.getProductName(), deviceInfo.getDeviceName(), deviceInfo.getTopic());
            deviceManager.deviceUnsubscribe(deviceInfo);
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
