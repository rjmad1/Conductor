package com.conductor.customer.api;

import com.conductor.customer.domain.CustomerContact;
import com.conductor.customer.service.ContactService;
import com.conductor.shared.customer.ContactType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/contacts")
@PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_PLATFORM_ADMIN')")
public class ContactController {

  private final ContactService contactService;

  public ContactController(ContactService contactService) {
    this.contactService = contactService;
  }

  @GetMapping
  public ResponseEntity<List<ContactResponse>> getContacts(@PathVariable UUID customerId) {
    List<ContactResponse> contacts =
        contactService.getContacts(customerId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    return ResponseEntity.ok(contacts);
  }

  @PostMapping
  public ResponseEntity<ContactResponse> addContact(
      @PathVariable UUID customerId, @Valid @RequestBody AddContactRequest request) {
    CustomerContact contact =
        contactService.addContact(
            customerId, request.type(), request.value(), request.label(), request.isPrimary());
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(contact));
  }

  @PutMapping("/{contactId}")
  public ResponseEntity<ContactResponse> updateContact(
      @PathVariable UUID customerId,
      @PathVariable UUID contactId,
      @Valid @RequestBody UpdateContactRequest request) {
    CustomerContact contact =
        contactService.updateContact(customerId, contactId, request.label(), request.isPrimary());
    return ResponseEntity.ok(toResponse(contact));
  }

  @PostMapping("/{contactId}/set-primary")
  public ResponseEntity<Void> setPrimaryContact(
      @PathVariable UUID customerId, @PathVariable UUID contactId) {
    contactService.setPrimaryContact(customerId, contactId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{contactId}")
  public ResponseEntity<Void> removeContact(
      @PathVariable UUID customerId, @PathVariable UUID contactId) {
    contactService.removeContact(customerId, contactId);
    return ResponseEntity.noContent().build();
  }

  private ContactResponse toResponse(CustomerContact contact) {
    return new ContactResponse(
        contact.getId(),
        contact.getCustomerId(),
        contact.getType(),
        contact.getValue(),
        contact.getValueHash(),
        contact.getLabel(),
        contact.isPrimary(),
        contact.isVerified(),
        contact.getVerifiedAt(),
        contact.getCreatedAt(),
        contact.getUpdatedAt());
  }

  public record AddContactRequest(
      @NotNull ContactType type, @NotBlank String value, String label, boolean isPrimary) {}

  public record UpdateContactRequest(String label, boolean isPrimary) {}

  public record ContactResponse(
      UUID id,
      UUID customerId,
      ContactType type,
      String value,
      String valueHash,
      String label,
      boolean isPrimary,
      boolean isVerified,
      Instant verifiedAt,
      Instant createdAt,
      Instant updatedAt) {}
}
