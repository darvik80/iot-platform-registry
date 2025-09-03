package xyz.crearts.iot.jsonrpc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для маркировки методов как JSON-RPC endpoints
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonRpcMethod {

    /**
     * Имя JSON-RPC метода. Если не указано, используется формат $className.$methodName
     */
    String value() default "";

    /**
     * Описание метода (опционально)
     */
    String description() default "";
}
