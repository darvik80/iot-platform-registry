package xyz.crearts.iot.registry.middlewate.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author ivan.kishchenko
 * email ivan.kishchenko@lazada.com
 */
@Getter
@SuperBuilder
public class DeviceConnectionEvent extends DeviceEvent {
    private String pid;
    private String macAddress;
    private String sessionId;
    private String clientId;
}
