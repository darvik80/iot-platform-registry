package xyz.crearts.registry.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import xyz.crearts.registry.repository.DeviceRepository;

/**
 * @author ivan.kishchenko
 * email ivan.kishchenko@lazada.com
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceManagement {
    private final DeviceRepository repository;

    @PostConstruct
    void setup() {
        var devices = repository.findAll();
        devices.forEach(device -> {
            log.info("{}:{}", device.getRegistry().getName(), device.getName());
        });
    }
}
