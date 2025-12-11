package com.easybilling.entity.rule;

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
 * Business Rule Entity.
 * Stores runtime-editable business rules for discounts, workflows, inventory, etc.
 */
@Entity
@Table(name = "business_rules", indexes = {
    @Index(name = "idx_rule_tenant", columnList = "tenant_id"),
    @Index(name = "idx_rule_type", columnList = "rule_type"),
    @Index(name = "idx_rule_active", columnList = "is_active"),
    @Index(name = "idx_rule_priority", columnList = "priority")
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@EntityListeners(TenantEntityListener.class)
public class BusinessRule implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "rule_name", nullable = false, length = 200)
    private String ruleName;
    
    @Column(name = "rule_type", nullable = false, length = 50)
    private String ruleType;  // 'discount', 'workflow', 'inventory', 'pricing', 'validation'
    
    @Column(length = 100)
    private String category;  // 'promotion', 'approval', 'stock', etc.
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 0;  // Higher = executes first
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "start_date")
    private Instant startDate;
    
    @Column(name = "end_date")
    private Instant endDate;
    
    /**
     * Rule conditions as JSON.
     * Defines when the rule should be applied.
     * Example: {
     *   "operator": "AND",
     *   "rules": [
     *     {"field": "totalAmount", "operator": "greaterThan", "value": 1000},
     *     {"field": "customerType", "operator": "equals", "value": "wholesale"}
     *   ]
     * }
     */
    @Column(nullable = false, columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> conditions;
    
    /**
     * Rule actions as JSON.
     * Defines what should happen when conditions are met.
     * Example: [
     *   {"type": "applyDiscount", "discountType": "percentage", "value": 10},
     *   {"type": "addMessage", "message": "10% discount applied!"}
     * ]
     */
    @Column(nullable = false, columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, Object>> actions;
    
    /**
     * Additional rule parameters as JSON.
     * Example: {"stackable": false, "maxDiscountAmount": 5000}
     */
    @Column(columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> parameters;
    
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
    
    /**
     * Check if rule is currently valid (within date range).
     */
    public boolean isValidNow() {
        Instant now = Instant.now();
        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }
        return isActive;
    }
}
