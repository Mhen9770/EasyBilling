package com.easybilling.filter;

import com.easybilling.context.TenantContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * Aspect that automatically enables Hibernate tenant filter for all repository operations.
 * This ensures that all queries are automatically filtered by the current tenant.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TenantFilterAspect {
    
    private final EntityManager entityManager;
    
    @Before("execution(* com.easybilling.repository..*(..))")
    public void enableTenantFilter() {
        Integer tenantId = TenantContext.getTenantId();
        
        if (tenantId != null) {
            Session session = entityManager.unwrap(Session.class);
            org.hibernate.Filter filter = session.enableFilter("tenantFilter");
            filter.setParameter("tenantId", tenantId);
            log.trace("Enabled tenant filter for tenantId: {}", tenantId);
        }
    }
}
