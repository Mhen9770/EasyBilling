package com.runtime.engine.workflow;

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
@Table(name = "workflow_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private String entityType;
    
    @Column(nullable = false)
    private String tenantId;
    
    @Column(nullable = false)
    private String trigger;
    
    @OneToMany(mappedBy = "workflowDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<WorkflowStep> steps = new ArrayList<>();
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> config;
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> metadata;
    
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
    
    public void addStep(WorkflowStep step) {
        steps.add(step);
        step.setWorkflowDefinition(this);
    }
    
    public void removeStep(WorkflowStep step) {
        steps.remove(step);
        step.setWorkflowDefinition(null);
    }
}
