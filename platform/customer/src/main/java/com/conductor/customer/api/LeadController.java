package com.conductor.customer.api;

import com.conductor.customer.domain.Customer;
import com.conductor.customer.service.ContactService;
import com.conductor.customer.service.CustomerMergeService;
import com.conductor.customer.service.CustomerService;
import com.conductor.customer.service.IdentityResolutionService;
import com.conductor.shared.customer.ContactType;
import com.conductor.shared.middleware.tenant.NatsEventPublisher;
import com.conductor.shared.middleware.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leads")
@PreAuthorize(
    "hasAnyAuthority('ROLE_TENANT_OWNER', 'ROLE_TENANT_ADMIN', 'ROLE_TENANT_AGENT', 'ROLE_PLATFORM_ADMIN')")
public class LeadController {

  private static final Logger log = LoggerFactory.getLogger(LeadController.class);

  private final CustomerService customerService;
  private final ContactService contactService;
  private final IdentityResolutionService identityResolutionService;
  private final CustomerMergeService customerMergeService;
  private final NatsEventPublisher eventPublisher;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public LeadController(
      CustomerService customerService,
      ContactService contactService,
      IdentityResolutionService identityResolutionService,
      CustomerMergeService customerMergeService,
      NatsEventPublisher eventPublisher) {
    this.customerService = customerService;
    this.contactService = contactService;
    this.identityResolutionService = identityResolutionService;
    this.customerMergeService = customerMergeService;
    this.eventPublisher = eventPublisher;
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> createLead(
      @Valid @RequestBody CreateLeadRequest request) {
    UUID tenantId = TenantContext.getCurrentTenantId();
    log.info("Received lead creation request for tenant: {}", tenantId);

    // 1. Identity Resolution
    Optional<UUID> resolvedEmailCustId =
        request.email() != null && !request.email().isBlank()
            ? identityResolutionService.resolveByEmail(request.email())
            : Optional.empty();

    Optional<UUID> resolvedPhoneCustId =
        request.phone() != null && !request.phone().isBlank()
            ? identityResolutionService.resolveByPhone(request.phone())
            : Optional.empty();

    UUID targetCustomerId = null;

    if (resolvedEmailCustId.isPresent() && resolvedPhoneCustId.isPresent()) {
      UUID emailId = resolvedEmailCustId.get();
      UUID phoneId = resolvedPhoneCustId.get();
      if (emailId.equals(phoneId)) {
        targetCustomerId = emailId;
      } else {
        Customer merged = customerMergeService.merge(emailId, phoneId);
        targetCustomerId = merged.getId();
      }
    } else if (resolvedEmailCustId.isPresent()) {
      targetCustomerId = resolvedEmailCustId.get();
      if (request.phone() != null && !request.phone().isBlank()) {
        try {
          contactService.addContact(
              targetCustomerId, ContactType.PHONE, request.phone(), "Lead Phone", true);
        } catch (Exception e) {
          log.warn("Failed to add phone contact: {}", e.getMessage());
        }
      }
    } else if (resolvedPhoneCustId.isPresent()) {
      targetCustomerId = resolvedPhoneCustId.get();
      if (request.email() != null && !request.email().isBlank()) {
        try {
          contactService.addContact(
              targetCustomerId, ContactType.EMAIL, request.email(), "Lead Email", true);
        } catch (Exception e) {
          log.warn("Failed to add email contact: {}", e.getMessage());
        }
      }
    } else {
      String displayName =
          (request.firstName() != null ? request.firstName() : "")
              + (request.lastName() != null ? " " + request.lastName() : "");
      if (displayName.trim().isEmpty()) {
        displayName = "Lead";
      }
      Customer customer =
          customerService.createCustomer(
              request.firstName(), request.lastName(), displayName.trim(), null, "LEAD_CAPTURE");
      targetCustomerId = customer.getId();

      if (request.email() != null && !request.email().isBlank()) {
        contactService.addContact(
            targetCustomerId, ContactType.EMAIL, request.email(), "Primary Email", true);
      }
      if (request.phone() != null && !request.phone().isBlank()) {
        contactService.addContact(
            targetCustomerId, ContactType.PHONE, request.phone(), "Primary Phone", true);
      }
    }

    try {
      Map<String, Object> payload =
          Map.of(
              "customerId", targetCustomerId.toString(),
              "firstName", request.firstName() != null ? request.firstName() : "",
              "lastName", request.lastName() != null ? request.lastName() : "",
              "email", request.email() != null ? request.email() : "",
              "phone", request.phone() != null ? request.phone() : "");
      String payloadJson = objectMapper.writeValueAsString(payload);
      eventPublisher.publishEvent("customer", "lead", "created", payloadJson);
      log.info("Published lead created event for customer: {}", targetCustomerId);
    } catch (Exception e) {
      log.error("Failed to publish lead created event", e);
    }

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Map.of("customerId", targetCustomerId.toString(), "status", "CREATED"));
  }

  public record CreateLeadRequest(String firstName, String lastName, String email, String phone) {}
}
