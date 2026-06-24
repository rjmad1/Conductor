package com.conductor.integrations.transformation;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class TransformationEngine {

    /**
     * Transforms a source payload using a set of mapping rules.
     * Mapping rules map target fields to source fields in dot notation (e.g., "email" -> "customer.email").
     */
    public Map<String, Object> transform(Map<String, Object> source, Map<String, String> rules) {
        if (source == null || rules == null) {
            return new HashMap<>();
        }
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, String> entry : rules.entrySet()) {
            String targetKey = entry.getKey();
            String sourcePath = entry.getValue();
            Object value = resolveValue(source, sourcePath);
            if (value != null) {
                result.put(targetKey, value);
            }
        }
        return result;
    }

    private Object resolveValue(Map<String, Object> source, String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        String[] parts = path.split("\\.");
        Object current = source;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }
}
