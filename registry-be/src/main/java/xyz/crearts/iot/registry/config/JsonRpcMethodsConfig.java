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

        // sum -> сумма чисел в params
        d.register("sum", params -> {
            // Поддержим как позиционные, так и именованные параметры
            if (params == null || params.isNull()) {
                throw new IllegalArgumentException("params required");
            }
            double total = 0;
            if (params.isArray()) {
                for (JsonNode n : params) total += n.asDouble();
            } else if (params.isObject()) {
                if (!params.has("values") || !params.get("values").isArray()) {
                    throw new IllegalArgumentException("object params must contain array 'values'");
                }
                for (JsonNode n : params.get("values")) total += n.asDouble();
            } else {
                throw new IllegalArgumentException("params must be array or object");
            }
            return new DoubleNode(total);
        });

        // echo -> возвращает params как есть
        d.register("echo", params -> params == null ? NullNode.getInstance() : params);

        return d;
    }
}
