package com.conductor.customer.api;

import com.conductor.customer.domain.ConsentRecord;
import com.conductor.customer.service.ConsentService;
import com.conductor.shared.customer.ConsentAction;
import com.conductor.shared.customer.ConsentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/consent")
@PreAuthorize("hasAnyRole('ROLE_TENANT_ADMIN', 'ROLE_PLATFORM_ADMIN')")
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @PostMapping("/grant")
    public ResponseEntity<ConsentResponse> grantConsent(@PathVariable UUID customerId,
                                                        @Valid @RequestBody GrantConsentRequest request) {
        ConsentRecord record = consentService.grantConsent(
                customerId,
                request.consentType(),
                request.channel(),
                request.legalBasis(),
                request.consentVersion(),
                request.ipAddress(),
                request.userAgent(),
                request.metadata()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(record));
    }

    @PostMapping("/revoke")
    public ResponseEntity<ConsentResponse> revokeConsent(@PathVariable UUID customerId,
                                                         @Valid @RequestBody RevokeConsentRequest request) {
        ConsentRecord record = consentService.revokeConsent(
                customerId,
                request.consentType(),
                request.channel(),
                request.consentVersion(),
                request.ipAddress(),
                request.userAgent(),
                request.metadata()
        );
        return ResponseEntity.ok(toResponse(record));
    }

    @GetMapping
    public ResponseEntity<List<ConsentStatusResponse>> getConsentStatus(@PathVariable UUID customerId) {
        List<ConsentStatusResponse> statuses = Arrays.stream(ConsentType.values())
                .map(type -> {
                    boolean active = consentService.isConsentActive(customerId, type);
                    return new ConsentStatusResponse(type, active);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ConsentResponse>> getConsentHistory(@PathVariable UUID customerId) {
        List<ConsentResponse> history = consentService.getConsentHistory(customerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }

    private ConsentResponse toResponse(ConsentRecord record) {
        return new ConsentResponse(
                record.getId(),
                record.getCustomerId(),
                record.getConsentType(),
                record.getAction(),
                record.getChannel(),
                record.getLegalBasis(),
                record.getConsentVersion(),
                record.getIpAddress(),
                record.getUserAgent(),
                record.getMetadata(),
                record.getCreatedAt()
        );
    }

    public record GrantConsentRequest(
            @NotNull ConsentType consentType,
            String channel,
            String legalBasis,
            @NotBlank String consentVersion,
            String ipAddress,
            String userAgent,
            String metadata
    ) {}

    public record RevokeConsentRequest(
            @NotNull ConsentType consentType,
            String channel,
            @NotBlank String consentVersion,
            String ipAddress,
            String userAgent,
            String metadata
    ) {}

    public record ConsentStatusResponse(
            ConsentType consentType,
            boolean isActive
    ) {}

    public record ConsentResponse(
            UUID id,
            UUID customerId,
            ConsentType consentType,
            ConsentAction action,
            String channel,
            String legalBasis,
            String consentVersion,
            String ipAddress,
            String userAgent,
            String metadata,
            Instant createdAt
    ) {}
}
