package com.easybilling.entity;

import com.easybilling.listener.TenantEntityListener;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity for authentication and authorization.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username", unique = true),
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_tenant", columnList = "tenant_id"),
        @Index(name = "idx_user_status", columnList = "status")
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@EntityListeners(TenantEntityListener.class)
public class User implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(nullable = false, length = 255)
    private String password; // BCrypt encrypted
    
    @Column(name = "first_name", length = 50)
    private String firstName;
    
    @Column(name = "last_name", length = 50)
    private String lastName;
    
    @Column(length = 20)
    private String phone;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<String> roles = new HashSet<>();
    
    @Column(name = "last_login")
    private Instant lastLogin;
    
    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;
    
    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "account_locked_until")
    private Instant accountLockedUntil;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by", length = 50)
    private String createdBy;
    
    public enum UserStatus {
        ACTIVE,
        INACTIVE,
        LOCKED,
        PENDING_VERIFICATION
    }
    
    /**
     * Check if user account is locked.
     */
    public boolean isAccountLocked() {
        if (accountLockedUntil == null) {
            return false;
        }
        return Instant.now().isBefore(accountLockedUntil);
    }
    
    /**
     * Check if user has a specific role.
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    /**
     * Add role to user.
     */
    public void addRole(String role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(role);
    }
}
