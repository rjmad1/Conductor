package com.conductor.customer;

import com.conductor.customer.domain.CustomerContact;
import com.conductor.customer.domain.CustomerIdentifier;
import com.conductor.customer.repository.CustomerContactRepository;
import com.conductor.customer.repository.CustomerIdentifierRepository;
import com.conductor.customer.service.IdentityResolutionService;
import com.conductor.shared.customer.ContactType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityResolutionServiceTest {

    @Mock
    private CustomerContactRepository contactRepository;

    @Mock
    private CustomerIdentifierRepository identifierRepository;

    private IdentityResolutionService identityResolutionService;

    @BeforeEach
    void setUp() {
        identityResolutionService = new IdentityResolutionService(contactRepository, identifierRepository);
    }

    @Test
    void testNormalizeEmail() {
        assertEquals("test@example.com", identityResolutionService.normalizeEmail("  TEST@example.com  "));
        assertEquals("", identityResolutionService.normalizeEmail(null));
    }

    @Test
    void testNormalizePhone() {
        assertEquals("+1234567890", identityResolutionService.normalizePhone("  +1-234-567-890  "));
        assertEquals("", identityResolutionService.normalizePhone(null));
    }

    @Test
    void testHashEmail() {
        String hash1 = identityResolutionService.hashEmail("  TEST@example.com  ");
        String hash2 = identityResolutionService.hashEmail("test@example.com");
        assertEquals(hash1, hash2);
        assertEquals(64, hash1.length());
    }

    @Test
    void testResolveByEmail() {
        UUID customerId = UUID.randomUUID();
        String email = "test@example.com";
        String hash = identityResolutionService.hashEmail(email);

        CustomerContact contact = new CustomerContact();
        contact.setCustomerId(customerId);
        contact.setValueHash(hash);

        when(contactRepository.findByTypeAndValueHash(ContactType.EMAIL, hash)).thenReturn(Optional.of(contact));

        Optional<UUID> resolved = identityResolutionService.resolveByEmail(email);
        assertTrue(resolved.isPresent());
        assertEquals(customerId, resolved.get());
    }

    @Test
    void testResolveByPhone() {
        UUID customerId = UUID.randomUUID();
        String phone = "+1234567890";
        String hash = identityResolutionService.hashPhone(phone);

        CustomerContact contact = new CustomerContact();
        contact.setCustomerId(customerId);
        contact.setValueHash(hash);

        when(contactRepository.findByTypeAndValueHash(ContactType.PHONE, hash)).thenReturn(Optional.of(contact));

        Optional<UUID> resolved = identityResolutionService.resolveByPhone(phone);
        assertTrue(resolved.isPresent());
        assertEquals(customerId, resolved.get());
    }

    @Test
    void testResolveByExternalId() {
        UUID customerId = UUID.randomUUID();
        String externalId = "crm-1234";
        String sourceSystem = "shopify";
        String identifierType = "EXTERNAL_ID:SHOPIFY";
        String hash = identityResolutionService.hashValue(externalId);

        CustomerIdentifier identifier = new CustomerIdentifier();
        identifier.setCustomerId(customerId);
        identifier.setIdentifierHash(hash);

        when(identifierRepository.findByIdentifierTypeAndIdentifierHash(identifierType, hash)).thenReturn(Optional.of(identifier));

        Optional<UUID> resolved = identityResolutionService.resolveByExternalId(externalId, sourceSystem);
        assertTrue(resolved.isPresent());
        assertEquals(customerId, resolved.get());
    }
}
