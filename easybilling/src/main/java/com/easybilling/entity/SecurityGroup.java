package com.easybilling.entity;

import com.easybilling.enums.Permission;
import com.easybilling.listener.TenantEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Security Group entity for managing permissions.
 * Staff users are assigned to security groups which define their permissions.
 */
@Entity
@Table(name = "security_groups", indexes = {
        @Index(name = "idx_security_group_tenant", columnList = "tenant_id"),
        @Index(name = "idx_security_group_name", columnList = "name"),
        @Index(name = "idx_security_group_status", columnList = "is_active")
})
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@EntityListeners(TenantEntityListener.class)
public class SecurityGroup implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "security_group_permissions", 
                     joinColumns = @JoinColumn(name = "security_group_id"))
    @Column(name = "permission")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by", length = 50)
    private String createdBy;
    
    @Column(name = "updated_by", length = 50)
    private String updatedBy;
    
    /**
     * Add permission to security group.
     */
    public void addPermission(Permission permission) {
        if (permissions == null) {
            permissions = new HashSet<>();
        }
        permissions.add(permission);
    }
    
    /**
     * Remove permission from security group.
     */
    public void removePermission(Permission permission) {
        if (permissions != null) {
            permissions.remove(permission);
        }
    }
    
    /**
     * Check if security group has a specific permission.
     */
    public boolean hasPermission(Permission permission) {
        return permissions != null && permissions.contains(permission);
    }
    
    /**
     * Add multiple permissions to security group.
     */
    public void addPermissions(Set<Permission> permissions) {
        if (this.permissions == null) {
            this.permissions = new HashSet<>();
        }
        this.permissions.addAll(permissions);
    }
}
