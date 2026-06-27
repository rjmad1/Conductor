package com.conductor.customer;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import com.conductor.customer.service.ConsentService;
import com.conductor.customer.service.IdentityResolutionService;
import com.conductor.customer.service.StopUnsubscribeListener;
import com.conductor.customer.service.StopUnsubscribeListener.InboundMessageContent;
import com.conductor.customer.service.StopUnsubscribeListener.InboundMessagePayload;
import com.conductor.shared.customer.ConsentType;
import com.conductor.shared.events.ConductorEvent;
import com.conductor.shared.messaging.EventConsumer;
import com.conductor.shared.middleware.tenant.TenantContext;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StopUnsubscribeListenerTest {

  @Mock private EventConsumer eventConsumer;

  @Mock private IdentityResolutionService identityResolutionService;

  @Mock private ConsentService consentService;

  private StopUnsubscribeListener listener;

  @BeforeEach
  void setUp() {
    listener =
        new StopUnsubscribeListener(eventConsumer, identityResolutionService, consentService);
  }

  @Test
  void testRegistrationOnStartup() {
    listener.afterSingletonsInstantiated();
    verify(eventConsumer)
        .subscribe(
            eq("customer_consent_stop_group"),
            eq("messaging"),
            eq("message"),
            eq("inbound"),
            isNull(),
            eq(InboundMessagePayload.class),
            any());
  }

  @Test
  void testHandleInboundMessage_WithStopKeyword_RevokesConsent() {
    UUID tenantId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    String fromPhone = "+919999999999";

    InboundMessagePayload payload = new InboundMessagePayload();
    payload.setFromPhone(fromPhone);
    payload.setCustomerId(customerId.toString());
    InboundMessageContent content = new InboundMessageContent();
    content.setText("STOP");
    payload.setContent(content);

    ConductorEvent<InboundMessagePayload> event =
        ConductorEvent.<InboundMessagePayload>builder()
            .tenantId(tenantId.toString())
            .payload(payload)
            .build();

    // Capture callback passed to subscribe
    ArgumentCaptor<java.util.function.Consumer<ConductorEvent<InboundMessagePayload>>> captor =
        ArgumentCaptor.forClass(java.util.function.Consumer.class);
    listener.afterSingletonsInstantiated();
    verify(eventConsumer).subscribe(any(), any(), any(), any(), any(), any(), captor.capture());

    // Trigger the handler callback
    captor.getValue().accept(event);

    verify(consentService)
        .revokeConsent(
            eq(customerId),
            eq(ConsentType.MARKETING),
            eq("WHATSAPP"),
            eq("v1"),
            eq("0.0.0.0"),
            eq("SYSTEM_STOP_INTERCEPTOR"),
            contains("stop_keyword"));
    assertNull(TenantContext.getCurrentTenantId());
  }

  @Test
  void testHandleInboundMessage_WithStopKeywordResolveByPhone_RevokesConsent() {
    UUID tenantId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    String fromPhone = "+919999999999";

    InboundMessagePayload payload = new InboundMessagePayload();
    payload.setFromPhone(fromPhone);
    InboundMessageContent content = new InboundMessageContent();
    content.setText("stop");
    payload.setContent(content);

    ConductorEvent<InboundMessagePayload> event =
        ConductorEvent.<InboundMessagePayload>builder()
            .tenantId(tenantId.toString())
            .payload(payload)
            .build();

    when(identityResolutionService.resolveByPhone(fromPhone)).thenReturn(Optional.of(customerId));

    ArgumentCaptor<java.util.function.Consumer<ConductorEvent<InboundMessagePayload>>> captor =
        ArgumentCaptor.forClass(java.util.function.Consumer.class);
    listener.afterSingletonsInstantiated();
    verify(eventConsumer).subscribe(any(), any(), any(), any(), any(), any(), captor.capture());

    captor.getValue().accept(event);

    verify(consentService)
        .revokeConsent(
            eq(customerId),
            eq(ConsentType.MARKETING),
            eq("WHATSAPP"),
            eq("v1"),
            eq("0.0.0.0"),
            eq("SYSTEM_STOP_INTERCEPTOR"),
            contains("stop_keyword"));
  }

  @Test
  void testHandleInboundMessage_WithOtherMessage_DoesNothing() {
    InboundMessagePayload payload = new InboundMessagePayload();
    payload.setFromPhone("+919999999999");
    InboundMessageContent content = new InboundMessageContent();
    content.setText("Hello");
    payload.setContent(content);

    ConductorEvent<InboundMessagePayload> event =
        ConductorEvent.<InboundMessagePayload>builder()
            .tenantId(UUID.randomUUID().toString())
            .payload(payload)
            .build();

    ArgumentCaptor<java.util.function.Consumer<ConductorEvent<InboundMessagePayload>>> captor =
        ArgumentCaptor.forClass(java.util.function.Consumer.class);
    listener.afterSingletonsInstantiated();
    verify(eventConsumer).subscribe(any(), any(), any(), any(), any(), any(), captor.capture());

    captor.getValue().accept(event);

    verifyNoInteractions(identityResolutionService);
    verifyNoInteractions(consentService);
  }
}
