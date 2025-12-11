package com.easybilling.repository;

import com.easybilling.entity.Notification;
import com.easybilling.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    
    Page<Notification> findByTenantId(Integer tenantId, Pageable pageable);
    
    Page<Notification> findByTenantIdAndStatus(Integer tenantId, NotificationStatus status, Pageable pageable);
    
    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer maxRetries);
    
    long countByTenantIdAndStatus(Integer tenantId, NotificationStatus status);
}
