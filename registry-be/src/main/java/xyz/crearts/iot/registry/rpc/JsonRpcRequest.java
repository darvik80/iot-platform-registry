// Java
package xyz.crearts.iot.registry.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcRequest {
    public String jsonrpc = "2.0";
    public String method;
    public JsonNode params; // может быть объект/массив/примитив
    public JsonNode id;     // может быть строкой/числом/NULL/отсутствовать (уведомление)
}
