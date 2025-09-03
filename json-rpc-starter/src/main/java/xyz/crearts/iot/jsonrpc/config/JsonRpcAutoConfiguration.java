package xyz.crearts.iot.jsonrpc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.socket.WebSocketHandler;
import xyz.crearts.iot.jsonrpc.rpc.JsonRpcDispatcher;

/**
 * Автоконфигурация для JSON-RPC Spring Boot Starter
 */
@AutoConfiguration
@ConditionalOnClass(WebSocketHandler.class)
@ConditionalOnWebApplication
@Import({
    JsonRpcWebSocketConfig.class,

})
public class JsonRpcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonRpcDispatcher jsonRpcDispatcher() {
        var jsonRpcDispatcher = new JsonRpcDispatcher();
        // ping -> "pong"
        jsonRpcDispatcher.register("ping", params -> new TextNode("pong"));

        // echo -> возвращает params как есть
        jsonRpcDispatcher.register("echo", params -> params == null ? NullNode.getInstance() : params);

        return jsonRpcDispatcher;
    }

    @Bean
    public JsonRpcWebSocketHandler jsonRpcWebSocketHandler(ObjectMapper objectMapper, JsonRpcDispatcher dispatcher) {
        return new JsonRpcWebSocketHandler(objectMapper, dispatcher);
    }

    @Bean
    public JsonRpcAutoRegistrar jsonRpcAutoRegistrar() {
        return new JsonRpcAutoRegistrar();
    }
}
