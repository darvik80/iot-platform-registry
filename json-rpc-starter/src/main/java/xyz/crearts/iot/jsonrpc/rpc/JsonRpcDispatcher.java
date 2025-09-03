package xyz.crearts.iot.jsonrpc.rpc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsonRpcDispatcher {
    private final Map<String, JsonRpcHandler> handlers = new ConcurrentHashMap<>();

    public void register(String method, JsonRpcHandler handler) {
        handlers.put(method, handler);
    }

    public JsonRpcHandler get(String method) {
        return handlers.get(method);
    }

    public boolean has(String method) {
        return handlers.containsKey(method);
    }
}
