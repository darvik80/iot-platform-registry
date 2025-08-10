// Java
package xyz.crearts.iot.registry.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class JsonRpcWebSocketConfig implements WebSocketConfigurer {

    private final JsonRpcWebSocketHandler jsonRpcWebSocketHandler;

    public JsonRpcWebSocketConfig(JsonRpcWebSocketHandler jsonRpcWebSocketHandler) {
        this.jsonRpcWebSocketHandler = jsonRpcWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(jsonRpcWebSocketHandler, "/ws-rpc")
                .setAllowedOriginPatterns("*"); // при необходимости ограничьте домены
    }
}