package xyz.crearts.iot.registry.middlewate.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author ivan.kishchenko
 * email ivan.kishchenko@lazada.com
 */
@Getter
@SuperBuilder
public class DeviceEvent {
    private DeviceEventType event;
    private String registryName;
    private String productName;
    private String deviceName;
    private long timestamp;
}
