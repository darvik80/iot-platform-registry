package xyz.crearts.iot.registry.middlewate.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author ivan.kishchenko
 * email ivan.kishchenko@lazada.com
 */
@Getter
@RequiredArgsConstructor
public enum DeviceEventType {
    DEVICE_UNKNOWN("unknown"),
    DEVICE_CONNECTED("connection.created"),
    DEVICE_DISCONNECTED("connection.closed"),
    DEVICE_SUBSCRIBED("binding.created"),
    DEVICE_UNSUBSCRIBED("binding.deleted");

    private final String value;

    static public DeviceEventType fromRoutingKey(String str) {
        if (DEVICE_CONNECTED.getValue().equals(str)) {
            return DEVICE_CONNECTED;
        }
        if (DEVICE_DISCONNECTED.getValue().equals(str)) {
            return DEVICE_DISCONNECTED;
        }

        if (DEVICE_SUBSCRIBED.getValue().equals(str)) {
            return DEVICE_SUBSCRIBED;
        }

        if (DEVICE_UNSUBSCRIBED.getValue().equals(str)) {
            return DEVICE_UNSUBSCRIBED;
        }

        return DEVICE_UNKNOWN;
    }
}
