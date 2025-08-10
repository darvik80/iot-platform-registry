// Java
package xyz.crearts.iot.registry.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcError {
    public int code;
    public String message;
    public JsonNode data;

    public JsonRpcError() {}
    public JsonRpcError(int code, String message, JsonNode data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // Коды из спецификации
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;
}
