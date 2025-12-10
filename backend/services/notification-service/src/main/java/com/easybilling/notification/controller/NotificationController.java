package com.easybilling.notification.controller;

import com.easybilling.notification.dto.NotificationRequest;
import com.easybilling.notification.entity.Notification;
import com.easybilling.notification.enums.NotificationStatus;
import com.easybilling.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendNotification(
            @Valid @RequestBody NotificationRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        log.info("Sending notification for tenant: {}", tenantId);
        
        notificationService.sendNotification(request, tenantId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Notification queued for sending");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) NotificationStatus status) {
        
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Notification> notifications;
        
        if (status != null) {
            notifications = notificationService.getNotificationsByStatus(tenantId, status, pageRequest);
        } else {
            notifications = notificationService.getNotifications(tenantId, pageRequest);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", notifications);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
