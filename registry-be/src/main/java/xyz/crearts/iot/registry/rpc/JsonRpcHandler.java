// Java
package xyz.crearts.iot.registry.rpc;

import com.fasterxml.jackson.databind.JsonNode;

@FunctionalInterface
public interface JsonRpcHandler {
    JsonNode handle(JsonNode params) throws Exception;
}
