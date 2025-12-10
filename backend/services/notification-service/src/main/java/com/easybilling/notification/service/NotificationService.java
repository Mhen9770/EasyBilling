package com.easybilling.notification.service;

import com.easybilling.notification.dto.NotificationRequest;
import com.easybilling.notification.entity.Notification;
import com.easybilling.notification.enums.NotificationStatus;
import com.easybilling.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    @Async
    @Transactional
    public void sendNotification(NotificationRequest request, String tenantId) {
        log.info("Sending {} notification to {} for tenant: {}", 
                request.getType(), request.getRecipient(), tenantId);
        
        Notification notification = Notification.builder()
                .tenantId(tenantId)
                .type(request.getType())
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .message(request.getMessage())
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .build();
        
        try {
            // TODO: Integrate with actual notification providers (SMS, Email, WhatsApp)
            // For now, just mark as sent
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            log.info("Notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
        }
        
        notificationRepository.save(notification);
    }
    
    @Transactional(readOnly = true)
    public Page<Notification> getNotifications(String tenantId, Pageable pageable) {
        return notificationRepository.findByTenantId(tenantId, pageable);
    }
    
    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsByStatus(String tenantId, NotificationStatus status, Pageable pageable) {
        return notificationRepository.findByTenantIdAndStatus(tenantId, status, pageable);
    }
}
