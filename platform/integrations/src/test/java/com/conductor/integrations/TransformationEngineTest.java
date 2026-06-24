package com.conductor.integrations;

import com.conductor.integrations.transformation.TransformationEngine;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class TransformationEngineTest {

    @Test
    public void testDotPathTransformation() {
        TransformationEngine engine = new TransformationEngine();

        Map<String, Object> source = new HashMap<>();
        Map<String, Object> customer = new HashMap<>();
        customer.put("email", "customer@example.com");
        customer.put("first_name", "Jane");
        customer.put("last_name", "Doe");
        source.put("customer", customer);
        source.put("id", 12345);

        Map<String, String> rules = new HashMap<>();
        rules.put("email", "customer.email");
        rules.put("firstName", "customer.first_name");
        rules.put("lastName", "customer.last_name");
        rules.put("orderId", "id");

        Map<String, Object> transformed = engine.transform(source, rules);

        assertNotNull(transformed);
        assertEquals("customer@example.com", transformed.get("email"));
        assertEquals("Jane", transformed.get("firstName"));
        assertEquals("Doe", transformed.get("lastName"));
        assertEquals(12345, transformed.get("orderId"));
    }
}
