package xyz.crearts.iot.registry.middlewate.event;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author ivan.kishchenko
 * email ivan.kishchenko@lazada.com
 */
@Getter
@SuperBuilder
public class DeviceSubscribeEvent  extends DeviceEvent {
    String topic;
    String module;
    String component;
}
