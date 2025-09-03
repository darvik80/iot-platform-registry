package xyz.crearts.iot.jsonrpc.rpc;

import com.fasterxml.jackson.databind.JsonNode;

@FunctionalInterface
public interface JsonRpcHandler {
    JsonNode handle(JsonNode params) throws Exception;
}
