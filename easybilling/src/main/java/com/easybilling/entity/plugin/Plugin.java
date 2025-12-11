package com.easybilling.entity.plugin;

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
import java.util.Map;

/**
 * Plugin Registry Entity.
 * Stores information about installed and available plugins.
 */
@Entity
@Table(name = "plugins", indexes = {
    @Index(name = "idx_plugin_tenant", columnList = "tenant_id"),
    @Index(name = "idx_plugin_code", columnList = "plugin_code"),
    @Index(name = "idx_plugin_enabled", columnList = "is_enabled")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_plugin_tenant_code", 
                     columnNames = {"tenant_id", "plugin_code"})
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@EntityListeners(TenantEntityListener.class)
public class Plugin implements TenantAware {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;
    
    @Column(name = "plugin_code", nullable = false, length = 100)
    private String pluginCode;  // 'tally_export', 'whatsapp_integration', 'shopify_sync'
    
    @Column(name = "plugin_name", nullable = false, length = 200)
    private String pluginName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 50)
    private String version;
    
    @Column(name = "plugin_type", nullable = false, length = 50)
    private String pluginType;  // 'integration', 'export', 'import', 'notification'
    
    @Column(length = 100)
    private String category;    // 'accounting', 'ecommerce', 'communication'
    
    @Column(name = "is_enabled")
    @Builder.Default
    private Boolean isEnabled = false;
    
    @Column(name = "is_system_plugin")
    @Builder.Default
    private Boolean isSystemPlugin = false;  // true for built-in plugins
    
    @Column(name = "implementation_class", length = 500)
    private String implementationClass;  // Fully qualified class name
    
    /**
     * Plugin configuration as JSON.
     * Stores API keys, endpoints, and other settings.
     * Example: {
     *   "apiKey": "xxx",
     *   "apiSecret": "yyy",
     *   "endpoint": "https://api.example.com",
     *   "syncInterval": "hourly"
     * }
     */
    @Column(columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> configuration;
    
    /**
     * Plugin metadata as JSON.
     * Additional information about the plugin.
     * Example: {
     *   "author": "EasyBilling Team",
     *   "website": "https://easybilling.com",
     *   "supportEmail": "support@easybilling.com",
     *   "dependencies": ["core"],
     *   "permissions": ["EXPORT_DATA", "SEND_NOTIFICATIONS"]
     * }
     */
    @Column(columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;
    
    @Column(name = "last_executed_at")
    private Instant lastExecutedAt;
    
    @Column(name = "execution_count")
    @Builder.Default
    private Long executionCount = 0L;
    
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
     * Increment execution counter.
     */
    public void incrementExecutionCount() {
        this.executionCount++;
        this.lastExecutedAt = Instant.now();
    }
}
