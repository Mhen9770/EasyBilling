package com.runtime.engine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.runtime.engine.util.JsonAttributesConverter;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "field_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_definition_id", nullable = false)
    private EntityDefinition entityDefinition;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String label;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private String dataType;
    
    @Column(nullable = false)
    private Boolean required = false;
    
    @Column(nullable = false)
    private Boolean unique = false;
    
    @Column(nullable = false)
    private Boolean indexed = false;
    
    @Column(nullable = false)
    private Boolean searchable = false;
    
    @Column(nullable = false)
    private Boolean displayInList = true;
    
    @Column(nullable = false)
    private Boolean displayInForm = true;
    
    private Integer minLength;
    
    private Integer maxLength;
    
    private String pattern;
    
    private String defaultValue;
    
    @Column(nullable = false)
    private Integer orderIndex = 0;
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> validationRules;
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> displayConfig;
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> metadata;
    
    private String referenceEntity;
    
    private String referenceField;
    
    @Column(nullable = false)
    private Boolean calculated = false;
    
    private String calculationExpression;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
