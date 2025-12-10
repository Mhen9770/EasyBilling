package com.easybilling.notification.repository;

import com.easybilling.notification.entity.Notification;
import com.easybilling.notification.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    
    Page<Notification> findByTenantId(String tenantId, Pageable pageable);
    
    Page<Notification> findByTenantIdAndStatus(String tenantId, NotificationStatus status, Pageable pageable);
    
    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer maxRetries);
    
    long countByTenantIdAndStatus(String tenantId, NotificationStatus status);
}
