package com.conductor.shared.middleware.tenant;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component
public class TenantContextFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TenantContextFilter.class);
    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String USER_HEADER = "X-User-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String tenantIdStr = httpRequest.getHeader(TENANT_HEADER);
            String userId = httpRequest.getHeader(USER_HEADER);

            if (tenantIdStr != null && !tenantIdStr.trim().isEmpty()) {
                try {
                    UUID tenantId = UUID.fromString(tenantIdStr);
                    TenantContext.setCurrentTenantId(tenantId);
                    TenantContext.setCurrentUserId(userId);
                    log.debug("Resolved Tenant: {} for User: {}", tenantId, userId);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid UUID format in X-Tenant-ID header: '{}'", tenantIdStr);
                    if (response instanceof HttpServletResponse httpResponse) {
                        httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Tenant ID format");
                        return;
                    }
                }
            } else {
                log.debug("No X-Tenant-ID header found in request: {}", httpRequest.getRequestURI());
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
