package com.easybilling.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing a tenant (merchant) in the platform.
 */
@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenant_slug", columnList = "slug", unique = true),
        @Index(name = "idx_tenant_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Tenant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 50)
    private String slug; // Used for subdomain (e.g., tenant1.easybilling.com)
    
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionPlan plan;
    
    @Column(name = "contact_email", nullable = false, length = 255)
    private String contactEmail;
    
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;
    
    @Column(length = 500)
    private String address;
    
    @Column(length = 50)
    private String city;
    
    @Column(length = 50)
    private String state;
    
    @Column(length = 50)
    private String country;
    
    @Column(name = "postal_code", length = 20)
    private String postalCode;
    
    @Column(name = "tax_number", length = 50)
    private String taxNumber; // GST/VAT number
    
    @Column(name = "subscription_start_date")
    private Instant subscriptionStartDate;
    
    @Column(name = "subscription_end_date")
    private Instant subscriptionEndDate;
    
    @Column(name = "trial_end_date")
    private Instant trialEndDate;
    
    @Column(name = "max_users")
    private Integer maxUsers;
    
    @Column(name = "max_stores")
    private Integer maxStores;
    
    @Column(name = "database_name", length = 100)
    private String databaseName; // For database-per-tenant strategy
    
    @Column(name = "schema_name", length = 100)
    private String schemaName; // For schema-per-tenant strategy
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by", length = 50)
    private String createdBy;
    
    public enum TenantStatus {
        PENDING,    // Awaiting setup
        TRIAL,      // In trial period
        ACTIVE,     // Active subscription
        SUSPENDED,  // Temporarily suspended
        CANCELLED   // Subscription cancelled
    }
    
    public enum SubscriptionPlan {
        BASIC,      // Basic features
        PRO,        // Professional features
        ENTERPRISE  // Enterprise features with customization
    }
}
