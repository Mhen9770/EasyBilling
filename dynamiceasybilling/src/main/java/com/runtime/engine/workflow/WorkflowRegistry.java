package com.runtime.engine.workflow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Component
@Slf4j
public class WorkflowRegistry {
    
    private final Map<String, WorkflowDefinition> workflows = new ConcurrentHashMap<>();
    private final Map<String, List<WorkflowDefinition>> workflowsByEntity = new ConcurrentHashMap<>();
    
    public void registerWorkflow(WorkflowDefinition workflow) {
        if (workflow == null || workflow.getName() == null) {
            throw new IllegalArgumentException("Workflow definition and name cannot be null");
        }
        
        workflows.put(workflow.getName(), workflow);
        
        workflowsByEntity.computeIfAbsent(workflow.getEntityType(), k -> new ArrayList<>())
            .add(workflow);
        
        log.info("Registered workflow: {} for entity: {}", workflow.getName(), workflow.getEntityType());
    }
    
    public void unregisterWorkflow(String workflowName) {
        WorkflowDefinition workflow = workflows.remove(workflowName);
        
        if (workflow != null) {
            List<WorkflowDefinition> entityWorkflows = workflowsByEntity.get(workflow.getEntityType());
            if (entityWorkflows != null) {
                entityWorkflows.remove(workflow);
            }
            log.info("Unregistered workflow: {}", workflowName);
        }
    }
    
    public Optional<WorkflowDefinition> getWorkflow(String workflowName) {
        return Optional.ofNullable(workflows.get(workflowName));
    }
    
    public List<WorkflowDefinition> getWorkflowsByEntity(String entityType) {
        return new ArrayList<>(workflowsByEntity.getOrDefault(entityType, new ArrayList<>()));
    }
    
    public List<WorkflowDefinition> getWorkflowsByTrigger(String entityType, String trigger) {
        return workflowsByEntity.getOrDefault(entityType, new ArrayList<>())
            .stream()
            .filter(w -> w.getTrigger().equals(trigger) && w.getActive())
            .toList();
    }
    
    public boolean isWorkflowRegistered(String workflowName) {
        return workflows.containsKey(workflowName);
    }
    
    public List<String> getAllWorkflowNames() {
        return new ArrayList<>(workflows.keySet());
    }
    
    public List<WorkflowDefinition> getAllWorkflows() {
        return new ArrayList<>(workflows.values());
    }
    
    public void clear() {
        workflows.clear();
        workflowsByEntity.clear();
        log.info("Workflow registry cleared");
    }
    
    public int getWorkflowCount() {
        return workflows.size();
    }
}
