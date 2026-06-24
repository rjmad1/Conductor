package com.conductor.shared.middleware.tenant;

import com.conductor.shared.messaging.EventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class NatsEventPublisher implements AutoCloseable {

    private final EventPublisher delegate;

    public NatsEventPublisher(EventPublisher delegate) {
        this.delegate = delegate;
    }

    public void publishEvent(String domain, String entity, String action, String payloadJson) {
        delegate.publish(domain, entity, action, "v1", payloadJson);
    }

    @Override
    public void close() {
        // No-op, lifecycle managed by container
    }
}
