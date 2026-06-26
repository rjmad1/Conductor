package com.conductor.customer.api;

import com.conductor.customer.domain.Customer;
import com.conductor.customer.service.CustomerMergeService;
import com.conductor.customer.service.CustomerSearchService;
import com.conductor.customer.service.CustomerService;
import com.conductor.shared.customer.CustomerStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_PLATFORM_ADMIN')")
public class CustomerController {

  private final CustomerService customerService;
  private final CustomerMergeService customerMergeService;
  private final CustomerSearchService customerSearchService;

  public CustomerController(
      CustomerService customerService,
      CustomerMergeService customerMergeService,
      CustomerSearchService customerSearchService) {
    this.customerService = customerService;
    this.customerMergeService = customerMergeService;
    this.customerSearchService = customerSearchService;
  }

  @PostMapping
  public ResponseEntity<CustomerResponse> createCustomer(
      @Valid @RequestBody CreateCustomerRequest request) {
    Customer customer =
        customerService.createCustomer(
            request.firstName(),
            request.lastName(),
            request.displayName(),
            request.externalId(),
            request.sourceSystem());
    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(customer));
  }

  @GetMapping("/{id}")
  public ResponseEntity<CustomerResponse> getCustomer(@PathVariable UUID id) {
    return customerService
        .findById(id)
        .map(customer -> ResponseEntity.ok(toResponse(customer)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<Page<CustomerResponse>> getAllCustomers(Pageable pageable) {
    Page<CustomerResponse> page = customerService.findAll(pageable).map(this::toResponse);
    return ResponseEntity.ok(page);
  }

  @PutMapping("/{id}")
  public ResponseEntity<CustomerResponse> updateCustomer(
      @PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequest request) {
    Customer customer =
        customerService.updateCustomer(
            id, request.firstName(), request.lastName(), request.displayName());
    return ResponseEntity.ok(toResponse(customer));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
    customerService.softDeleteCustomer(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/deactivate")
  public ResponseEntity<Void> deactivateCustomer(@PathVariable UUID id) {
    customerService.deactivateCustomer(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/archive")
  public ResponseEntity<Void> archiveCustomer(@PathVariable UUID id) {
    customerService.archiveCustomer(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/merge")
  public ResponseEntity<CustomerResponse> mergeCustomers(
      @PathVariable UUID id, @RequestParam UUID targetId) {
    Customer merged = customerMergeService.merge(id, targetId);
    return ResponseEntity.ok(toResponse(merged));
  }

  @GetMapping("/search")
  public ResponseEntity<List<CustomerResponse>> searchCustomers(
      @RequestParam(required = false) String phone,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) UUID tagId,
      @RequestParam(required = false) UUID segmentId) {
    List<CustomerResponse> results =
        customerSearchService.search(phone, email, name, tagId, segmentId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    return ResponseEntity.ok(results);
  }

  private CustomerResponse toResponse(Customer customer) {
    return new CustomerResponse(
        customer.getId(),
        customer.getTenantId(),
        customer.getExternalId(),
        customer.getSourceSystem(),
        customer.getFirstName(),
        customer.getLastName(),
        customer.getDisplayName(),
        customer.getStatus(),
        customer.getMergedIntoId(),
        customer.getAttributes(),
        customer.getCreatedAt(),
        customer.getUpdatedAt());
  }

  public record CreateCustomerRequest(
      String firstName,
      String lastName,
      @NotBlank String displayName,
      String externalId,
      String sourceSystem) {}

  public record UpdateCustomerRequest(
      String firstName, String lastName, @NotBlank String displayName) {}

  public record CustomerResponse(
      UUID id,
      UUID tenantId,
      String externalId,
      String sourceSystem,
      String firstName,
      String lastName,
      String displayName,
      CustomerStatus status,
      UUID mergedIntoId,
      String attributes,
      Instant createdAt,
      Instant updatedAt) {}
}
