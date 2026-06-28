# Connector Framework

Conductor exposes a canonical interface structure `ConnectorAdapter` which maps to third-party CRM, e-commerce, and billing platforms.

## Interface Signature

```java
public interface ConnectorAdapter {
    String getConnectorType();
    String getVersion();
    void connect(UUID tenantId, Map<String, Object> params);
    void disconnect(UUID tenantId);
    boolean testConnection(UUID tenantId, Map<String, Object> credentials);
    Object execute(UUID tenantId, String action, Map<String, Object> payload);
    void subscribe(UUID tenantId, String eventName, String webhookUrl);
    void unsubscribe(UUID tenantId, String eventName);
    void refreshToken(UUID tenantId);
    boolean healthCheck(UUID tenantId);
}
```

## Registry Injection
The registry dynamic scanner auto-binds all instances on boot:
```java
@Component
public class ConnectorRegistry { ... }
```
