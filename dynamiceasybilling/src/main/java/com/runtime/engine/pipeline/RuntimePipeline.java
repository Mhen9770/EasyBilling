package com.runtime.engine.pipeline;

import com.runtime.engine.entity.EntityEngine;
import com.runtime.engine.permission.PermissionEngine;
import com.runtime.engine.plugin.PluginEngine;
import com.runtime.engine.rule.RuleDefinition;
import com.runtime.engine.rule.RuleEngine;
import com.runtime.engine.repo.RuleDefinitionRepository;
import com.runtime.engine.workflow.WorkflowDefinition;
import com.runtime.engine.workflow.WorkflowEngine;
import com.runtime.engine.repo.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RuntimePipeline {
    
    private final EntityEngine entityEngine;
    private final RuleEngine ruleEngine;
    private final WorkflowEngine workflowEngine;
    private final PluginEngine pluginEngine;
    private final PermissionEngine permissionEngine;
    private final RuleDefinitionRepository ruleDefinitionRepository;
    private final WorkflowRepository workflowRepository;
    
    public Object execute(String action, String entityType, Map<String, Object> data, RuntimeExecutionContext context) {
        log.info("Executing runtime pipeline for action: {}, entity: {}", action, entityType);
        
        try {
            context.setCurrentEntity(entityType);
            context.setCurrentAction(action);
            
            executePreProcessing(action, entityType, data, context);
            
            Object result = executeAction(action, entityType, data, context);
            
            executePostProcessing(action, entityType, data, context);
            
            return result;
        } catch (Exception e) {
            log.error("Error executing runtime pipeline", e);
            throw new PipelineExecutionException("Failed to execute pipeline", e);
        }
    }
    
    private void executePreProcessing(String action, String entityType, Map<String, Object> data, RuntimeExecutionContext context) {
        log.debug("Executing pre-processing");
        
        permissionEngine.enforcePermission(entityType, action, context);
        
        pluginEngine.executePlugins("pre_" + action, data, context);
        
        List<RuleDefinition> preRules = ruleDefinitionRepository
            .findByTriggerAndEntityTypeAndTenantIdAndActive("pre_" + action, entityType, context.getTenantId(), true);
        ruleEngine.executeRules(preRules, data, context);
        
        List<WorkflowDefinition> preWorkflows = workflowRepository
            .findByTriggerAndEntityTypeAndTenantId("pre_" + action, entityType, context.getTenantId());
        for (WorkflowDefinition workflow : preWorkflows) {
            if (workflow.getActive()) {
                workflowEngine.executeWorkflow(workflow, data, context);
            }
        }
    }
    
    private Object executeAction(String action, String entityType, Map<String, Object> data, RuntimeExecutionContext context) {
        log.debug("Executing action: {}", action);
        
        return switch (action.toLowerCase()) {
            case "create" -> entityEngine.createEntity(entityType, data, context);
            case "update" -> {
                Long entityId = context.getCurrentEntityId();
                if (entityId == null && data.containsKey("id")) {
                    entityId = ((Number) data.get("id")).longValue();
                }
                yield entityEngine.updateEntity(entityId, entityType, data, context);
            }
            case "find" -> entityEngine.findByQuery(entityType, data, context);
            case "delete" -> {
                Long entityId = context.getCurrentEntityId();
                if (entityId == null && data.containsKey("id")) {
                    entityId = ((Number) data.get("id")).longValue();
                }
                entityEngine.deleteEntity(entityId, context);
                yield Map.of("success", true);
            }
            default -> executeCustomAction(action, entityType, data, context);
        };
    }
    
    private Object executeCustomAction(String action, String entityType, Map<String, Object> data, RuntimeExecutionContext context) {
        log.debug("Executing custom action: {}", action);
        
        pluginEngine.executePlugins(action, data, context);
        
        List<RuleDefinition> actionRules = ruleDefinitionRepository
            .findByTriggerAndEntityTypeAndTenantIdAndActive(action, entityType, context.getTenantId(), true);
        ruleEngine.executeRules(actionRules, data, context);
        
        return Map.of("success", true, "action", action);
    }
    
    private void executePostProcessing(String action, String entityType, Map<String, Object> data, RuntimeExecutionContext context) {
        log.debug("Executing post-processing");
        
        pluginEngine.executePlugins("post_" + action, data, context);
        
        List<RuleDefinition> postRules = ruleDefinitionRepository
            .findByTriggerAndEntityTypeAndTenantIdAndActive("post_" + action, entityType, context.getTenantId(), true);
        ruleEngine.executeRules(postRules, data, context);
        
        List<WorkflowDefinition> postWorkflows = workflowRepository
            .findByTriggerAndEntityTypeAndTenantId("post_" + action, entityType, context.getTenantId());
        for (WorkflowDefinition workflow : postWorkflows) {
            if (workflow.getActive()) {
                workflowEngine.executeWorkflow(workflow, data, context);
            }
        }
    }
    
    public static class PipelineExecutionException extends RuntimeException {
        public PipelineExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
