package com.easybilling.repository;

import com.easybilling.entity.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookRepository extends JpaRepository<Webhook, Long> {
    
    List<Webhook> findByTenantIdAndEventTypeAndIsActiveTrue(Integer tenantId, String eventType);
    
    List<Webhook> findByTenantIdAndIsActiveTrue(Integer tenantId);
    
    List<Webhook> findByTenantId(Integer tenantId);
}
