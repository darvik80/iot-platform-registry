package xyz.crearts.iot.registry.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.crearts.iot.registry.entity.DeviceDO;
import xyz.crearts.iot.registry.entity.DeviceStatus;
import xyz.crearts.iot.registry.entity.RegistryDO;
import xyz.crearts.iot.registry.repository.DeviceRepository;
import xyz.crearts.iot.registry.repository.RegistryRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceManager {
    private final String SYSTEM_USER = "system";

    private final RegistryRepository registryRepository;
    private final DeviceRepository deviceRepository;

    @PostConstruct
    public void postConstruct() {
        deviceRepository.findAll().forEach(device -> {
            log.info("Device: {}, {}", device.getName(), device.getId());
            log.info("\tRegistry: {}", device.getRegistry().getName());
        });
    }

    public void deviceOnline(String productName, String clientId, String sessionId) {
        try {
            var registry = registryRepository.findFirstByName(productName);
            if (null == registry) {
                var newRegistry = new RegistryDO();
                newRegistry.setName(productName);
                newRegistry.setCreatedBy(SYSTEM_USER);
                registry = registryRepository.save(newRegistry);
            }

            var device = deviceRepository.findFirstByClientIdAndRegistryId(clientId, registry.getId());
            if (device == null) {
                device = new DeviceDO();
                device.setName(clientId);
                device.setClientId(clientId);
                device.setRegistry(registry);
                device.setCreatedBy(SYSTEM_USER);
                device.setUuid(UUID.randomUUID().toString());
            } else {
                device.setUpdatedBy(SYSTEM_USER);
            }
            device.setStatus(DeviceStatus.ONLINE);
            device.setSessionId(sessionId);
            deviceRepository.saveAndFlush(device);
        } catch (Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
        }
    }

    public void deviceOffline(String sessionId, long timestamp) {
        try {
            var device = deviceRepository.findFirstBySessionId(sessionId);
            if (null != device && device.getStatus() == DeviceStatus.ONLINE && device.getUpdatedAt().getTime() < timestamp) {
                device.setStatus(DeviceStatus.OFFLINE);
                device.setSessionId(sessionId);
                device.setUpdatedBy(SYSTEM_USER);
                deviceRepository.save(device);
            }
        } catch (Throwable ignored) {}
    }
}
