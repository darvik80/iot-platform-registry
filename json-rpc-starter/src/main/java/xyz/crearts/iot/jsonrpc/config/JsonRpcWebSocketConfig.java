// Java
package xyz.crearts.iot.jsonrpc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Конфигурация WebSocket для JSON-RPC
 */
@Configuration
@EnableWebSocket
@ConditionalOnProperty(name = "jsonrpc.websocket.enabled", havingValue = "true", matchIfMissing = true)
public class JsonRpcWebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private JsonRpcWebSocketHandler jsonRpcWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(jsonRpcWebSocketHandler, "/jsonrpc")
                .setAllowedOrigins("*"); // В продакшене настройте корректно
    }
}