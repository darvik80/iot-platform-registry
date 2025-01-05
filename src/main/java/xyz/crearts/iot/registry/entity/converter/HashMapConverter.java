package xyz.crearts.iot.registry.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Converter
public class HashMapConverter implements AttributeConverter<Map<String, Object>, String> {
    final private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> customerInfo) {

        if (customerInfo == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(customerInfo);
        } catch (final JsonProcessingException e) {
            return "{}";
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String customerInfoJSON) {

        if (customerInfoJSON == null || customerInfoJSON.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> customerInfo = null;
        try {
            customerInfo = objectMapper.readValue(customerInfoJSON,
                    new TypeReference<HashMap<String, Object>>() {});
        } catch (final IOException e) {
            //log.info("JSON reading error", e);
        }

        return customerInfo;
    }
}