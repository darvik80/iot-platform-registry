package xyz.crearts.iot.registry.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import xyz.crearts.iot.registry.entity.DeviceDO;
import xyz.crearts.iot.registry.entity.DeviceStatus;
import xyz.crearts.iot.registry.entity.ProductDO;
import xyz.crearts.iot.registry.entity.RegistryDO;
import xyz.crearts.iot.registry.middlewate.event.DeviceConnectionEvent;
import xyz.crearts.iot.registry.middlewate.event.DeviceSubscribeEvent;
import xyz.crearts.iot.registry.repository.DeviceRepository;
import xyz.crearts.iot.registry.repository.ProductRepository;
import xyz.crearts.iot.registry.repository.RegistryRepository;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceManager {
    private final String SYSTEM_USER = "system";
    public final String SYS_TOPIC_CONFIG = "sys.config";
    public final String SYS_TOPIC_TELEMETRY = "sys.telemetry";

    private final ObjectMapper mapper = new ObjectMapper();

    private final RabbitTemplate rmqTemplate;
    private final RegistryRepository registryRepository;
    private final ProductRepository productRepository;
    private final DeviceRepository deviceRepository;

    public void deviceOnline(DeviceConnectionEvent info) {
        try {
            var registry = registryRepository.findFirstByName(info.getRegistryName());
            if (null == registry) {
                var newRegistry = new RegistryDO();
                newRegistry.setName(info.getProductName());
                newRegistry.setCreatedBy(SYSTEM_USER);

                registry = registryRepository.save(newRegistry);
            }

            var product = productRepository.findFirstByNameAndRegistryId(info.getProductName(), registry.getId());
            if (null == product) {
                var newProduct = new ProductDO();
                newProduct.setName(info.getProductName());
                newProduct.setCreatedBy(SYSTEM_USER);
                newProduct.setRegistry(registry);

                product = productRepository.save(newProduct);
            }

            var device = deviceRepository.findFirstByNameAndProductId(info.getDeviceName(), product.getId());
            if (device == null) {
                device = new DeviceDO();
                device.setName(info.getDeviceName());
                device.setClientId(info.getSessionId());
                device.setProduct(product);
                device.setCreatedBy(SYSTEM_USER);
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

    public void deviceSubscribe(DeviceSubscribeEvent event) {
        var device = deviceRepository.findFirstByNameAndProduct_Name(event.getDeviceName(), event.getProductName());
        if (device != null) {
            log.info("Device {}:{} subscribed: {}", event.getProductName(), event.getDeviceName(), event.getTopic());
            if (event.getModule().equals("sys") && event.getComponent().equals("config")) {
                deviceSendConfig(device);
            }
        }

    }

    public void deviceUnsubscribe(DeviceSubscribeEvent event) {
        var device = deviceRepository.findFirstByNameAndProduct_Name(event.getDeviceName(), event.getProductName());
        if (device != null) {
            log.info("Device {}:{} unsubscribed: {}", event.getProductName(), event.getDeviceName(), event.getTopic());
        }
    }

    public void deviceSendConfig(String deviceName, String productName) {
        var device = deviceRepository.findFirstByNameAndProduct_Name(deviceName, productName);
        if (device != null) {
            deviceSendConfig(device);
        }
    }

    private void deviceSendConfig(DeviceDO device) {
        try {
            var payload = mapper.writeValueAsString(device.getConfiguration()).getBytes(StandardCharsets.UTF_8);
            var props = new MessageProperties();
            props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            String cfgTopic = String.format(".%s.%s.%s", device.getProduct().getName(), device.getName(), SYS_TOPIC_CONFIG);
            rmqTemplate.send("amq.topic", cfgTopic, new Message(payload, props));
        } catch (Throwable throwable) {
            log.error("Can't send config", throwable);
        }
    }
}
