package com.easybilling.entity.workflow;

import com.easybilling.entity.TenantAware;
import com.easybilling.listener.TenantEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Workflow Definition Entity.
 * Stores configurable workflow definitions for POS, approvals, returns, etc.
 */
@Entity
@Table(name = "workflow_definitions", indexes = {
    @Index(name = "idx_workflow_tenant", columnList = "tenant_id"),
    @Index(name = "idx_workflow_code", columnList = "workflow_code"),
    @Index(name = "idx_workflow_active", columnList = "is_active")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_workflow_tenant_code", 
                     columnNames = {"tenant_id", "workflow_code"})
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@EntityListeners(TenantEntityListener.class)
public class WorkflowDefinition implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "workflow_code", nullable = false, length = 100)
    private String workflowCode;  // 'pos_checkout', 'invoice_approval', 'return_process'
    
    @Column(name = "workflow_name", nullable = false, length = 200)
    private String workflowName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "workflow_type", nullable = false, length = 50)
    private String workflowType;  // 'pos', 'approval', 'return', 'stock_transfer'
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    /**
     * Workflow states as JSON array.
     * Defines all possible states in the workflow.
     * Example: [
     *   {"code": "draft", "label": "Draft", "isInitial": true},
     *   {"code": "pending_approval", "label": "Pending Approval"},
     *   {"code": "approved", "label": "Approved", "isFinal": true}
     * ]
     */
    @Column(nullable = false, columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> states;
    
    /**
     * Workflow transitions as JSON array.
     * Defines allowed state transitions and their conditions.
     * Example: [
     *   {
     *     "from": "draft",
     *     "to": "pending_approval",
     *     "action": "submit",
     *     "label": "Submit for Approval",
     *     "conditions": {...},
     *     "requiredPermission": "SUBMIT_INVOICE"
     *   }
     * ]
     */
    @Column(nullable = false, columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> transitions;
    
    /**
     * Workflow actions/hooks as JSON.
     * Actions to execute at each state or transition.
     * Example: {
     *   "onEnter": {"pending_approval": [{"type": "sendNotification", ...}]},
     *   "onExit": {...},
     *   "onTransition": {...}
     * }
     */
    @Column(columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> actions;
    
    /**
     * Workflow configuration as JSON.
     * Additional settings and parameters.
     * Example: {
     *   "timeout": "24h",
     *   "autoEscalation": true,
     *   "escalationTo": "supervisor@company.com"
     * }
     */
    @Column(columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> configuration;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}
