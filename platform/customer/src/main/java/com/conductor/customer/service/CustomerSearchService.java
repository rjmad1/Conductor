package com.conductor.customer.service;

import com.conductor.customer.domain.Customer;
import com.conductor.customer.repository.CustomerContactRepository;
import com.conductor.customer.repository.CustomerRepository;
import com.conductor.customer.repository.CustomerSegmentRepository;
import com.conductor.customer.repository.CustomerTagRepository;
import com.conductor.shared.customer.ContactType;
import com.conductor.shared.middleware.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

/**
 * CustomerSearchService — multi-field customer search, tenant-scoped.
 *
 * Search strategies:
 *   1. Phone search  → identity resolution via value_hash (O(1), exact)
 *   2. Email search  → identity resolution via value_hash (O(1), exact)
 *   3. Name search   → PostgreSQL full-text search via tsvector (approximate)
 *   4. Tag search    → join via customer_tags
 *   5. Segment search→ join via customer_segments
 *
 * All queries are tenant-scoped by the Hibernate @Filter (TenantAwareEntity).
 * No cross-tenant results are ever returned.
 *
 * Results are de-duplicated using LinkedHashSet to preserve ranking order
 * when multiple search strategies yield the same customer.
 */
@Service
public class CustomerSearchService {

    private static final Logger log = LoggerFactory.getLogger(CustomerSearchService.class);

    private final CustomerRepository        customerRepository;
    private final CustomerContactRepository contactRepository;
    private final CustomerTagRepository     tagRepository;
    private final CustomerSegmentRepository segmentRepository;
    private final IdentityResolutionService identityResolutionService;

    public CustomerSearchService(
            CustomerRepository customerRepository,
            CustomerContactRepository contactRepository,
            CustomerTagRepository tagRepository,
            CustomerSegmentRepository segmentRepository,
            IdentityResolutionService identityResolutionService) {
        this.customerRepository        = customerRepository;
        this.contactRepository         = contactRepository;
        this.tagRepository             = tagRepository;
        this.segmentRepository         = segmentRepository;
        this.identityResolutionService = identityResolutionService;
    }

    /** Exact phone lookup via hash. */
    public List<Customer> searchByPhone(String phone) {
        String hash = identityResolutionService.hashPhone(
                identityResolutionService.normalizePhone(phone));
        return contactRepository.findByTypeAndValueHash(ContactType.PHONE, hash)
                .flatMap(c -> customerRepository.findById(c.getCustomerId()))
                .map(List::of)
                .orElse(List.of());
    }

    /** Exact email lookup via hash. */
    public List<Customer> searchByEmail(String email) {
        String hash = identityResolutionService.hashEmail(
                identityResolutionService.normalizeEmail(email));
        return contactRepository.findByTypeAndValueHash(ContactType.EMAIL, hash)
                .flatMap(c -> customerRepository.findById(c.getCustomerId()))
                .map(List::of)
                .orElse(List.of());
    }

    /** Full-text name search using PostgreSQL tsvector. */
    public List<Customer> searchByName(String query) {
        UUID tenantId = TenantContext.getCurrentTenantId();
        if (tenantId == null || query == null || query.isBlank()) {
            return List.of();
        }
        return customerRepository.searchByDisplayName(tenantId.toString(), query.trim());
    }

    /** Customers with a specific tag. */
    public List<Customer> searchByTag(UUID tagId) {
        List<UUID> customerIds = tagRepository.findCustomerIdsByTagId(tagId);
        return customerRepository.findAllById(customerIds);
    }

    /** Customers in a specific segment. */
    public List<Customer> searchBySegment(UUID segmentId) {
        List<UUID> customerIds = segmentRepository.findCustomerIdsBySegmentId(segmentId);
        return customerRepository.findAllById(customerIds);
    }

    /**
     * Multi-field search: runs applicable strategies based on provided params.
     * Results are merged and de-duplicated. Order: phone > email > name > tag > segment.
     */
    public List<Customer> search(String phone, String email, String name,
                                  UUID tagId, UUID segmentId) {
        LinkedHashSet<Customer> results = new LinkedHashSet<>();

        if (phone != null && !phone.isBlank()) {
            results.addAll(searchByPhone(phone));
        }
        if (email != null && !email.isBlank()) {
            results.addAll(searchByEmail(email));
        }
        if (name != null && !name.isBlank()) {
            results.addAll(searchByName(name));
        }
        if (tagId != null) {
            results.addAll(searchByTag(tagId));
        }
        if (segmentId != null) {
            results.addAll(searchBySegment(segmentId));
        }

        log.debug("Customer search returned {} results", results.size());
        return new ArrayList<>(results);
    }
}
