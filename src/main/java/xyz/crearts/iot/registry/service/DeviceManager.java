package xyz.crearts.iot.registry.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import xyz.crearts.iot.registry.entity.DeviceDO;
import xyz.crearts.iot.registry.entity.DeviceStatus;
import xyz.crearts.iot.registry.entity.RegistryDO;
import xyz.crearts.iot.registry.middlewate.event.DeviceConnectionEvent;
import xyz.crearts.iot.registry.middlewate.event.DeviceSubscribeEvent;
import xyz.crearts.iot.registry.repository.DeviceRepository;
import xyz.crearts.iot.registry.repository.RegistryRepository;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceManager {
    private final String SYSTEM_USER = "system";

    private final ObjectMapper mapper = new ObjectMapper();

    private final RabbitTemplate rmqTemplate;
    private final RegistryRepository registryRepository;
    private final DeviceRepository deviceRepository;

    public void deviceOnline(DeviceConnectionEvent info) {
        try {
            var registry = registryRepository.findFirstByName(info.getProductName());
            if (null == registry) {
                var newRegistry = new RegistryDO();
                newRegistry.setName(info.getProductName());
                newRegistry.setCreatedBy(SYSTEM_USER);
                newRegistry.setUuid(UUID.randomUUID().toString());
                registry = registryRepository.save(newRegistry);
            }

            var device = deviceRepository.findFirstByNameAndRegistryId(info.getDeviceName(), registry.getId());
            if (device == null) {
                device = new DeviceDO();
                device.setName(info.getDeviceName());
                device.setClientId(info.getSessionId());
                device.setRegistry(registry);
                device.setCreatedBy(SYSTEM_USER);
                device.setUuid(UUID.randomUUID().toString());
            } else {
                device.setUpdatedBy(SYSTEM_USER);
            }
            device.setStatus(DeviceStatus.ONLINE);
            device.setSessionId(info.getSessionId());
            device.setClientId(info.getClientId());
            deviceRepository.saveAndFlush(device);
        } catch (Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
        }
    }

    public void deviceOffline(DeviceConnectionEvent info) {
        try {
            var device = deviceRepository.findFirstBySessionId(info.getSessionId());
            if (null != device && device.getStatus() == DeviceStatus.ONLINE && device.getUpdatedAt().getTime() < info.getTimestamp()) {
                device.setStatus(DeviceStatus.OFFLINE);
                device.setUpdatedBy(SYSTEM_USER);
                deviceRepository.save(device);
            }
        } catch (Throwable ignored) {
        }
    }

    @SneakyThrows
    public void deviceSubscribe(DeviceSubscribeEvent event) {
        var device = deviceRepository.findFirstByNameAndRegistry_Name(event.getDeviceName(), event.getProductName());
        if (device != null) {
            log.info("Device {}:{} subscribed: {}", event.getProductName(), event.getDeviceName(), event.getTopic());
            if (event.getModule().equals("sys") && event.getComponent().equals("config")) {
                var payload = mapper.writeValueAsString(device.getConfiguration()).getBytes(StandardCharsets.UTF_8);
                var props = new MessageProperties();
                props.setContentType(MessageProperties.CONTENT_TYPE_JSON);

                rmqTemplate.send("amq.topic", new Message(payload, props));
            }
        }

    }

    public void deviceUnsubscribe(DeviceSubscribeEvent event) {
        var device = deviceRepository.findFirstByNameAndRegistry_Name(event.getDeviceName(), event.getProductName());
        if (device != null) {
            log.info("Device {}:{} unsubscribed: {}", event.getProductName(), event.getDeviceName(), event.getTopic());
        }
    }
}
