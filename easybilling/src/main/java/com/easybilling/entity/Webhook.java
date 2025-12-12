package com.easybilling.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing webhook configurations.
 * Allows tenants to configure webhooks for external integrations.
 */
@Entity
@Table(name = "webhooks", indexes = {
        @Index(name = "idx_webhook_tenant", columnList = "tenant_id"),
        @Index(name = "idx_webhook_event", columnList = "event_type"),
        @Index(name = "idx_webhook_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Webhook implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "webhook_name", nullable = false, length = 100)
    private String webhookName;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType; // INVOICE_CREATED, INVOICE_PAID, PAYMENT_RECEIVED, CUSTOMER_CREATED, etc.
    
    @Column(name = "target_url", nullable = false, length = 500)
    private String targetUrl;
    
    @Column(name = "http_method", length = 10)
    private String httpMethod = "POST"; // POST, PUT, PATCH
    
    @Column(name = "headers", columnDefinition = "TEXT")
    private String headers; // JSON: {"Authorization": "Bearer token", "Content-Type": "application/json"}
    
    @Column(name = "payload_template", columnDefinition = "TEXT")
    private String payloadTemplate; // Custom JSON template with merge fields
    
    @Column(name = "secret_key", length = 100)
    private String secretKey; // For HMAC signature verification
    
    @Column(name = "retry_count")
    private Integer retryCount = 3;
    
    @Column(name = "retry_interval")
    private Integer retryInterval = 300; // seconds
    
    @Column(name = "timeout")
    private Integer timeout = 30; // seconds
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "last_triggered_at")
    private Instant lastTriggeredAt;
    
    @Column(name = "success_count")
    private Long successCount = 0L;
    
    @Column(name = "failure_count")
    private Long failureCount = 0L;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
}
