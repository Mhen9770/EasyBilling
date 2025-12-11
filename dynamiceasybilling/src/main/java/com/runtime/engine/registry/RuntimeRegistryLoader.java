package com.runtime.engine.registry;

import com.runtime.engine.entity.EntityDefinition;
import com.runtime.engine.entity.EntityRegistry;
import com.runtime.engine.repo.EntityDefinitionRepository;
import com.runtime.engine.workflow.WorkflowDefinition;
import com.runtime.engine.workflow.WorkflowRegistry;
import com.runtime.engine.repo.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class RuntimeRegistryLoader implements CommandLineRunner {
    
    private final EntityDefinitionRepository entityDefinitionRepository;
    private final EntityRegistry entityRegistry;
    private final WorkflowRepository workflowRepository;
    private final WorkflowRegistry workflowRegistry;
    
    @Override
    public void run(String... args) {
        log.info("Loading runtime registries...");
        
        loadEntityDefinitions();
        loadWorkflowDefinitions();
        
        log.info("Runtime registries loaded successfully");
    }
    
    private void loadEntityDefinitions() {
        try {
            List<EntityDefinition> definitions = entityDefinitionRepository.findAll();
            
            for (EntityDefinition definition : definitions) {
                if (definition.getActive()) {
                    entityRegistry.registerEntity(definition);
                }
            }
            
            log.info("Loaded {} entity definitions", definitions.size());
        } catch (Exception e) {
            log.error("Error loading entity definitions", e);
        }
    }
    
    private void loadWorkflowDefinitions() {
        try {
            List<WorkflowDefinition> workflows = workflowRepository.findAll();
            
            for (WorkflowDefinition workflow : workflows) {
                if (workflow.getActive()) {
                    workflowRegistry.registerWorkflow(workflow);
                }
            }
            
            log.info("Loaded {} workflow definitions", workflows.size());
        } catch (Exception e) {
            log.error("Error loading workflow definitions", e);
        }
    }
    
    public void reloadAll() {
        log.info("Reloading all registries...");
        
        entityRegistry.clear();
        workflowRegistry.clear();
        
        loadEntityDefinitions();
        loadWorkflowDefinitions();
        
        log.info("All registries reloaded");
    }
    
    public void reloadEntityDefinitions() {
        log.info("Reloading entity definitions...");
        entityRegistry.clear();
        loadEntityDefinitions();
    }
    
    public void reloadWorkflowDefinitions() {
        log.info("Reloading workflow definitions...");
        workflowRegistry.clear();
        loadWorkflowDefinitions();
    }
}
