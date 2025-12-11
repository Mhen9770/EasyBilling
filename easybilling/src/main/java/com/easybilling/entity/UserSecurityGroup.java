package com.easybilling.entity;

import com.easybilling.listener.TenantEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;

import java.time.Instant;

/**
 * Entity representing the many-to-many relationship between Users and Security Groups.
 * Staff users can belong to multiple security groups.
 */
@Entity
@Table(name = "user_security_groups", indexes = {
        @Index(name = "idx_user_security_group_user", columnList = "user_id"),
        @Index(name = "idx_user_security_group_group", columnList = "security_group_id"),
        @Index(name = "idx_user_security_group_tenant", columnList = "tenant_id")
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"userId", "securityGroupId"})
@EntityListeners(TenantEntityListener.class)
public class UserSecurityGroup implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    
    @Column(name = "security_group_id", nullable = false, length = 36)
    private String securityGroupId;
    
    @Column(nullable = false, name = "tenant_id")
    private Integer tenantId;
    
    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;
    
    @Column(name = "assigned_by", length = 50)
    private String assignedBy;
}
