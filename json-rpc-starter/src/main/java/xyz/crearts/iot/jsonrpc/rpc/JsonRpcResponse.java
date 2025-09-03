package xyz.crearts.iot.jsonrpc.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcResponse {
    public String jsonrpc = "2.0";
    public JsonNode result;
    public JsonRpcError error;
    public JsonNode id;

    public static JsonRpcResponse success(JsonNode id, JsonNode result) {
        JsonRpcResponse r = new JsonRpcResponse();
        r.id = id;
        r.result = result;
        return r;
    }

    public static JsonRpcResponse error(JsonNode id, int code, String message, JsonNode data) {
        JsonRpcResponse r = new JsonRpcResponse();
        r.id = id;
        r.error = new JsonRpcError(code, message, data);
        return r;
    }
}
