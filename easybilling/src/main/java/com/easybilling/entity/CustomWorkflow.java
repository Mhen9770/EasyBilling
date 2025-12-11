package com.easybilling.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing custom business workflows.
 * Allows tenants to define automated workflows with conditions and actions.
 */
@Entity
@Table(name = "custom_workflows", indexes = {
        @Index(name = "idx_workflow_tenant", columnList = "tenant_id"),
        @Index(name = "idx_workflow_trigger", columnList = "trigger_event"),
        @Index(name = "idx_workflow_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CustomWorkflow implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "workflow_name", nullable = false, length = 100)
    private String workflowName;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "trigger_event", nullable = false, length = 50)
    private String triggerEvent; // INVOICE_CREATED, INVOICE_PAID, CUSTOMER_CREATED, LOW_STOCK, etc.
    
    @Column(name = "conditions", columnDefinition = "TEXT")
    private String conditions; // JSON: [{field: "amount", operator: ">", value: 10000}]
    
    @Column(name = "actions", columnDefinition = "TEXT")
    private String actions; // JSON: [{type: "SEND_EMAIL", config: {...}}, {type: "CREATE_TASK", config: {...}}]
    
    @Column(name = "execution_order")
    private Integer executionOrder = 0; // Order of execution if multiple workflows for same trigger
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "last_executed_at")
    private Instant lastExecutedAt;
    
    @Column(name = "execution_count")
    private Long executionCount = 0L;
    
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
