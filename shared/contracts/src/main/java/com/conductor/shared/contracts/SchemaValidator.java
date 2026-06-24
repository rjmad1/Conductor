package com.conductor.shared.contracts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@Component
public class SchemaValidator {

    private static final Logger log = LoggerFactory.getLogger(SchemaValidator.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    
    private String schemasPath = "config/schemas";

    public void setSchemasPath(String path) {
        this.schemasPath = path;
    }

    public boolean validate(String domain, String entity, String action, String schemaVersion, String payloadJson) {
        try {
            String schemaFileName = String.format("%s.%s.%s.%s.json", domain, entity, action, schemaVersion);
            Path path = Paths.get(schemasPath, schemaFileName);
            
            InputStream schemaStream;
            if (Files.exists(path)) {
                schemaStream = new FileInputStream(path.toFile());
            } else {
                // Try classpath just in case for tests
                schemaStream = getClass().getClassLoader().getResourceAsStream("schemas/" + schemaFileName);
                if (schemaStream == null) {
                    log.error("Schema file not found at: {} or on classpath: schemas/{}", path.toAbsolutePath(), schemaFileName);
                    throw new IllegalArgumentException("Schema not found: " + schemaFileName);
                }
            }

            JsonSchema schema = schemaFactory.getSchema(schemaStream);
            JsonNode payloadNode = objectMapper.readTree(payloadJson);
            
            Set<ValidationMessage> assertions = schema.validate(payloadNode);
            if (!assertions.isEmpty()) {
                log.warn("Schema validation failed for event {}.{}.{} (version {}). Errors: {}", 
                        domain, entity, action, schemaVersion, assertions);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Error validating event payload against schema: {}.{}.{}", domain, entity, action, e);
            return false;
        }
    }
}
