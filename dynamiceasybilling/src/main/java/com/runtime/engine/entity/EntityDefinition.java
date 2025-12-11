package com.runtime.engine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.runtime.engine.util.JsonAttributesConverter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "entity_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private String tableName;
    
    @Column(nullable = false)
    private String tenantId;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(nullable = false)
    private Boolean auditable = false;
    
    @Column(nullable = false)
    private Boolean versionable = false;
    
    @OneToMany(mappedBy = "entityDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<FieldDefinition> fields = new ArrayList<>();
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> metadata;
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> validationRules;
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> displayConfig;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void addField(FieldDefinition field) {
        fields.add(field);
        field.setEntityDefinition(this);
    }
    
    public void removeField(FieldDefinition field) {
        fields.remove(field);
        field.setEntityDefinition(null);
    }
}
