package xyz.crearts.iot.jsonrpc.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import xyz.crearts.iot.jsonrpc.annotations.JsonRpcMethod;
import xyz.crearts.iot.jsonrpc.rpc.JsonRpcDispatcher;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Компонент для автоматической регистрации JSON-RPC методов, помеченных аннотацией @JsonRpcMethod
 * Поддерживает различные сигнатуры методов: R fn(T), R fn(), void fn(T)
 */
@Slf4j
public class JsonRpcAutoRegistrar {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JsonRpcDispatcher jsonRpcDispatcher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void registerAnnotatedMethods() {
        log.info("Начинаем автоматическую регистрацию JSON-RPC методов...");

        Map<String, Object> beans = applicationContext.getBeansOfType(Object.class);

        for (Object bean : beans.values()) {
            Class<?> clazz = bean.getClass();

            // Пропускаем proxy классы Spring
            if (clazz.getName().contains("$$")) {
                clazz = clazz.getSuperclass();
            }

            for (Method method : clazz.getDeclaredMethods()) {
                JsonRpcMethod annotation = method.getAnnotation(JsonRpcMethod.class);
                if (annotation != null) {
                    registerMethod(bean, method, annotation);
                }
            }
        }

        log.info("Автоматическая регистрация JSON-RPC методов завершена");
    }

    private void registerMethod(Object bean, Method method, JsonRpcMethod annotation) {
        String methodName = generateMethodName(bean.getClass(), method, annotation);

        try {
            Class<?> returnType = method.getReturnType();
            Parameter[] parameters = method.getParameters();

            // Проверяем поддерживаемые сигнатуры
            if (parameters.length > 1) {
                log.warn("Метод {} в классе {} имеет неподдерживаемую сигнатуру для JSON-RPC. " +
                           "Поддерживается максимум 1 параметр", 
                           method.getName(), bean.getClass().getSimpleName());
                return;
            }

            jsonRpcDispatcher.register(methodName, params -> {
                try {
                    Object result = invokeMethod(bean, method, params, parameters);
                    return convertResultToJsonNode(result, returnType);
                } catch (Exception e) {
                    log.error("Ошибка при выполнении JSON-RPC метода {}: {}", methodName, e.getMessage(), e);
                    throw new RuntimeException("Ошибка выполнения метода " + methodName, e);
                }
            });

            String signature = buildMethodSignature(method);
            log.info("Зарегистрирован JSON-RPC метод: {} -> {}.{} ({})",
                       methodName, bean.getClass().getSimpleName(), method.getName(), signature);

        } catch (Exception e) {
            log.error("Ошибка при регистрации JSON-RPC метода {}: {}", methodName, e.getMessage(), e);
        }
    }

    private String generateMethodName(Class<?> clazz, Method method, JsonRpcMethod annotation) {
        if (!annotation.value().isEmpty()) {
            return annotation.value();
        }

        // Получаем простое имя класса без суффиксов Spring proxy
        String className = clazz.getSimpleName();
        if (className.contains("$$")) {
            className = clazz.getSuperclass().getSimpleName();
        }

        return className + "." + method.getName();
    }

    private Object invokeMethod(Object bean, Method method, JsonNode params, Parameter[] parameters) throws Exception {
        if (parameters.length == 0) {
            // R fn() - метод без параметров
            return method.invoke(bean);
        } else {
            // R fn(T) или void fn(T) - метод с одним параметром
            Parameter parameter = parameters[0];
            Object convertedParam = convertJsonNodeToParameter(params, parameter.getType());
            return method.invoke(bean, convertedParam);
        }
    }

    private Object convertJsonNodeToParameter(JsonNode params, Class<?> parameterType) {
        if (params == null) {
            return null;
        }

        if (JsonNode.class.isAssignableFrom(parameterType)) {
            return params;
        }

        try {
            return objectMapper.treeToValue(params, parameterType);
        } catch (Exception e) {
            log.warn("Не удалось преобразовать параметр {} в тип {}: {}",
                       params, parameterType.getSimpleName(), e.getMessage());
            throw new RuntimeException("Ошибка преобразования параметра", e);
        }
    }

    private JsonNode convertResultToJsonNode(Object result, Class<?> returnType) {
        if (returnType == void.class || returnType == Void.class) {
            // void методы возвращают успешный статус
            return objectMapper.createObjectNode().put("success", true);
        }

        if (result == null) {
            return objectMapper.nullNode();
        }

        if (result instanceof JsonNode) {
            return (JsonNode) result;
        }

        try {
            return objectMapper.valueToTree(result);
        } catch (Exception e) {
            log.warn("Не удалось преобразовать результат {} в JsonNode: {}",
                       result, e.getMessage());
            return objectMapper.createObjectNode()
                .put("error", "Ошибка преобразования результата")
                .put("value", result.toString());
        }
    }

    private String buildMethodSignature(Method method) {
        StringBuilder signature = new StringBuilder();
        Class<?> returnType = method.getReturnType();

        if (returnType == void.class) {
            signature.append("void");
        } else {
            signature.append(returnType.getSimpleName());
        }

        signature.append(" ").append(method.getName()).append("(");

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                signature.append(", ");
            }
            signature.append(parameters[i].getType().getSimpleName());
        }

        signature.append(")");
        return signature.toString();
    }
}
