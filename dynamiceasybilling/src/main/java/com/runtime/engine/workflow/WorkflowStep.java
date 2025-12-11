package com.runtime.engine.workflow;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.runtime.engine.util.JsonAttributesConverter;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "workflow_steps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStep {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private String stepType;
    
    @Column(nullable = false)
    private Integer stepOrder;
    
    @Column(columnDefinition = "TEXT")
    private String condition;
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> config;
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> actions;
    
    @Column(nullable = false)
    private Boolean mandatory = true;
    
    @Column(nullable = false)
    private Boolean async = false;
    
    private String nextStepOnSuccess;
    
    private String nextStepOnFailure;
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> metadata;
    
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
