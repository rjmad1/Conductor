package com.conductor.shared.middleware.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Aspect
@Component
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* org.springframework.data.repository.Repository+.*(..))")
    public void enableTenantFilter() {
        UUID tenantId = TenantContext.getCurrentTenantId();
        if (tenantId != null) {
            try {
                Session session = entityManager.unwrap(Session.class);
                if (session != null) {
                    session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
                }
            } catch (Exception e) {
                // Log and swallow or handle appropriately depending on context (e.g. non-Hibernate EntityManager)
            }
        }
    }
}
