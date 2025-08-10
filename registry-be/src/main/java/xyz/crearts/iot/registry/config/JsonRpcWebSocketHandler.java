// Java
package xyz.crearts.iot.registry.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import xyz.crearts.iot.registry.rpc.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonRpcWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonRpcDispatcher dispatcher;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        try {
            JsonNode node = objectMapper.readTree(payload);
            if (node.isArray()) {
                handleBatch(session, (ArrayNode) node);
            } else if (node.isObject()) {
                handleSingle(session, (ObjectNode) node);
            } else {
                sendError(session, null, JsonRpcError.INVALID_REQUEST, "Invalid Request type", null);
            }
        } catch (JsonProcessingException e) {
            sendError(session, null, JsonRpcError.PARSE_ERROR, "Parse error", textNode(e.getOriginalMessage()));
        }
    }

    private void handleBatch(WebSocketSession session, ArrayNode array) throws IOException {
        List<JsonNode> responses = new ArrayList<>();
        for (JsonNode item : array) {
            if (!item.isObject()) {
                responses.add(errorResponse(null, JsonRpcError.INVALID_REQUEST, "Invalid Request in batch", null));
                continue;
            }
            ObjectNode obj = (ObjectNode) item;
            JsonNode id = obj.get("id");
            JsonNode response = processRequest(obj);
            if (response != null && !(id == null || id.isNull())) {
                responses.add(response);
            }
        }
        // Если все — уведомления, ответ не отправляем
        if (!responses.isEmpty()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(responses)));
        }
    }

    private void handleSingle(WebSocketSession session, ObjectNode obj) throws IOException {
        JsonNode id = obj.get("id");
        JsonNode response = processRequest(obj);
        // Для уведомления (без id) ответа нет
        if (response != null && !(id == null || id.isNull())) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }
    }

    private JsonNode processRequest(ObjectNode obj) {
        String version = text(obj.get("jsonrpc"));
        if (!"2.0".equals(version)) {
            return errorResponse(obj.get("id"), JsonRpcError.INVALID_REQUEST, "jsonrpc must be '2.0'", null);
        }
        String method = text(obj.get("method"));
        if (method == null || method.isEmpty()) {
            return errorResponse(obj.get("id"), JsonRpcError.INVALID_REQUEST, "Missing method", null);
        }
        JsonNode params = obj.get("params");
        if (!dispatcher.has(method)) {
            return errorResponse(obj.get("id"), JsonRpcError.METHOD_NOT_FOUND, "Method not found: " + method, null);
        }
        try {
            JsonNode result = dispatcher.get(method).handle(params);
            return successResponse(obj.get("id"), result == null ? NullNode.getInstance() : result);
        } catch (IllegalArgumentException e) {
            return errorResponse(obj.get("id"), JsonRpcError.INVALID_PARAMS, "Invalid params", textNode(e.getMessage()));
        } catch (Exception e) {
            log.error("JSON-RPC handler error for method {}", method, e);
            return errorResponse(obj.get("id"), JsonRpcError.INTERNAL_ERROR, "Internal error", textNode(e.getMessage()));
        }
    }

    private void sendError(WebSocketSession session, JsonNode id, int code, String message, JsonNode data) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse(id, code, message, data))));
    }

    private ObjectNode successResponse(JsonNode id, JsonNode result) {
        ObjectNode resp = objectMapper.createObjectNode();
        resp.put("jsonrpc", "2.0");
        resp.set("result", result);
        resp.set("id", id == null ? NullNode.getInstance() : id);
        return resp;
    }

    private ObjectNode errorResponse(JsonNode id, int code, String message, JsonNode data) {
        ObjectNode resp = objectMapper.createObjectNode();
        resp.put("jsonrpc", "2.0");
        ObjectNode error = objectMapper.createObjectNode();
        error.put("code", code);
        error.put("message", message);
        if (data != null) error.set("data", data);
        resp.set("error", error);
        resp.set("id", id == null ? NullNode.getInstance() : id);
        return resp;
    }

    private static String text(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }
    private static TextNode textNode(String s) {
        return s == null ? null : new TextNode(s);
    }
}
