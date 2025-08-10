// Java
package xyz.crearts.iot.registry.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.crearts.iot.registry.rpc.JsonRpcDispatcher;

@Configuration
public class JsonRpcMethodsConfig {

    private final ObjectMapper mapper = new ObjectMapper();

    @Bean
    public JsonRpcDispatcher jsonRpcDispatcher() {
        JsonRpcDispatcher d = new JsonRpcDispatcher();

        // ping -> "pong"
        d.register("ping", params -> new TextNode("pong"));

        // echo -> возвращает params как есть
        d.register("echo", params -> params == null ? NullNode.getInstance() : params);

        return d;
    }
}
