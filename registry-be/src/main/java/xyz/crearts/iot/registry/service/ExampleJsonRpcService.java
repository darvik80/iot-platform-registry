package xyz.crearts.iot.registry.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.stereotype.Service;
import xyz.crearts.iot.jsonrpc.annotations.JsonRpcMethod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Пример сервиса с JSON-RPC методами демонстрирующий различные сигнатуры:
 * R fn(T), R fn(), void fn(T)
 */
@Service
public class ExampleJsonRpcService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ===== JsonNode методы (оригинальные) =====

    @JsonRpcMethod("getTime")
    public JsonNode getCurrentTime() {
        return new TextNode(LocalDateTime.now().toString());
    }

    @JsonRpcMethod("uppercase")
    public JsonNode toUpperCase(JsonNode params) {
        if (params == null || !params.isTextual()) {
            return objectMapper.createObjectNode()
                .put("error", "Параметр должен быть строкой");
        }
        return new TextNode(params.asText().toUpperCase());
    }

    @JsonRpcMethod(value = "multiply", description = "Умножает число на 2")
    public JsonNode multiplyByTwo(JsonNode params) {
        if (params == null || !params.isNumber()) {
            return objectMapper.createObjectNode()
                .put("error", "Параметр должен быть числом");
        }
        return objectMapper.valueToTree(params.asDouble() * 2);
    }

    @JsonRpcMethod
    public JsonNode status() {
        return objectMapper.createObjectNode()
            .put("status", "OK")
            .put("timestamp", System.currentTimeMillis());
    }

    // ===== Generic методы с типизированными параметрами =====

    // String fn() - метод без параметров, возвращающий String
    @JsonRpcMethod("getServerInfo")
    public String getServerInfo() {
        return "IoT Platform Registry v1.0";
    }

    // String fn(String) - метод с String параметром
    @JsonRpcMethod("reverseString")
    public String reverseString(String text) {
        if (text == null) {
            return null;
        }
        return new StringBuilder(text).reverse().toString();
    }

    // Integer fn(Integer) - метод с числовым параметром
    @JsonRpcMethod("square")
    public Integer square(Integer number) {
        if (number == null) {
            return null;
        }
        return number * number;
    }

    // Double fn() - метод без параметров, возвращающий число
    @JsonRpcMethod("randomNumber")
    public Double getRandomNumber() {
        return Math.random() * 100;
    }

    // Boolean fn(String) - метод возвращающий булево значение
    @JsonRpcMethod("isValidEmail")
    public Boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return email.contains("@") && email.contains(".");
    }

    // List<String> fn() - метод возвращающий список
    @JsonRpcMethod("getSupportedMethods")
    public List<String> getSupportedMethods() {
        return List.of("getTime", "uppercase", "multiply", "status", 
                      "getServerInfo", "reverseString", "square", "randomNumber");
    }

    // Map<String, Object> fn(String) - метод возвращающий мапу
    @JsonRpcMethod("getUserProfile")
    public Map<String, Object> getUserProfile(String userId) {
        return Map.of(
            "userId", userId != null ? userId : "anonymous",
            "role", "user",
            "lastLogin", LocalDateTime.now().toString(),
            "permissions", List.of("read", "write")
        );
    }

    // ===== Void методы =====

    // void fn(String) - метод без возвращаемого значения
    @JsonRpcMethod("logMessage")
    public void logMessage(String message) {
        System.out.println("[JSON-RPC LOG] " + LocalDateTime.now() + ": " + message);
    }

    // void fn() - метод без параметров и возвращаемого значения
    @JsonRpcMethod("clearCache")
    public void clearCache() {
        System.out.println("[JSON-RPC] Cache cleared at " + LocalDateTime.now());
    }

    // ===== Пользовательские объекты =====

    public static class UserRequest {
        public String name;
        public String email;
        public Integer age;
    }

    public static class UserResponse {
        public Long id;
        public String name;
        public String email;
        public Integer age;
        public String status;
        public String createdAt;

        public UserResponse(String name, String email, Integer age) {
            this.id = System.currentTimeMillis();
            this.name = name;
            this.email = email;
            this.age = age;
            this.status = "active";
            this.createdAt = LocalDateTime.now().toString();
        }
    }

    // UserResponse fn(UserRequest) - метод с пользовательскими типами
    @JsonRpcMethod("createUser")
    public UserResponse createUser(UserRequest request) {
        if (request == null || request.name == null || request.email == null) {
            throw new IllegalArgumentException("Имя и email обязательны");
        }
        return new UserResponse(request.name, request.email, request.age);
    }
}
