package com.runtime.engine.workflow;

import com.runtime.engine.pipeline.RuntimeExecutionContext;
import com.runtime.engine.rule.ActionExecutor;
import com.runtime.engine.rule.ConditionEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowEngine {
    
    private final ConditionEvaluator conditionEvaluator;
    private final ActionExecutor actionExecutor;
    private final WorkflowRegistry workflowRegistry;
    
    public void executeWorkflow(WorkflowDefinition workflow, Map<String, Object> data, RuntimeExecutionContext context) {
        if (workflow == null || !workflow.getActive()) {
            log.warn("Workflow is null or not active");
            return;
        }
        
        log.info("Executing workflow: {}", workflow.getName());
        
        try {
            context.setMetadata("workflowId", workflow.getId());
            context.setMetadata("workflowName", workflow.getName());
            
            if (workflow.getSteps() == null || workflow.getSteps().isEmpty()) {
                log.warn("No steps defined for workflow: {}", workflow.getName());
                return;
            }
            
            for (WorkflowStep step : workflow.getSteps()) {
                executeStep(step, data, context);
                
                if (context.isValidationFailed() && step.getMandatory()) {
                    log.error("Mandatory step failed: {}, stopping workflow", step.getName());
                    break;
                }
            }
            
            log.info("Workflow execution completed: {}", workflow.getName());
        } catch (Exception e) {
            log.error("Error executing workflow: {}", workflow.getName(), e);
            throw new WorkflowExecutionException("Failed to execute workflow: " + workflow.getName(), e);
        }
    }
    
    public void executeStep(WorkflowStep step, Map<String, Object> data, RuntimeExecutionContext context) {
        log.debug("Executing workflow step: {}", step.getName());
        
        try {
            if (step.getCondition() != null && !step.getCondition().isEmpty()) {
                boolean conditionMet = conditionEvaluator.evaluate(step.getCondition(), data, context);
                
                if (!conditionMet) {
                    log.debug("Condition not met for step: {}, skipping", step.getName());
                    return;
                }
            }
            
            context.setMetadata("currentStep", step.getName());
            context.setMetadata("stepType", step.getStepType());
            
            if (step.getActions() != null && !step.getActions().isEmpty()) {
                actionExecutor.execute(step.getActions(), data, context);
            }
            
            executeStepType(step, data, context);
            
            log.debug("Step executed successfully: {}", step.getName());
        } catch (Exception e) {
            log.error("Error executing step: {}", step.getName(), e);
            
            if (step.getMandatory()) {
                throw new WorkflowExecutionException("Failed to execute mandatory step: " + step.getName(), e);
            }
        }
    }
    
    private void executeStepType(WorkflowStep step, Map<String, Object> data, RuntimeExecutionContext context) {
        String stepType = step.getStepType();
        
        switch (stepType.toLowerCase()) {
            case "validation" -> executeValidationStep(step, data, context);
            case "transformation" -> executeTransformationStep(step, data, context);
            case "enrichment" -> executeEnrichmentStep(step, data, context);
            case "notification" -> executeNotificationStep(step, data, context);
            case "custom" -> executeCustomStep(step, data, context);
            default -> log.debug("Unknown step type: {}", stepType);
        }
    }
    
    private void executeValidationStep(WorkflowStep step, Map<String, Object> data, RuntimeExecutionContext context) {
        log.debug("Executing validation step: {}", step.getName());
    }
    
    private void executeTransformationStep(WorkflowStep step, Map<String, Object> data, RuntimeExecutionContext context) {
        log.debug("Executing transformation step: {}", step.getName());
    }
    
    private void executeEnrichmentStep(WorkflowStep step, Map<String, Object> data, RuntimeExecutionContext context) {
        log.debug("Executing enrichment step: {}", step.getName());
    }
    
    private void executeNotificationStep(WorkflowStep step, Map<String, Object> data, RuntimeExecutionContext context) {
        log.debug("Executing notification step: {}", step.getName());
    }
    
    private void executeCustomStep(WorkflowStep step, Map<String, Object> data, RuntimeExecutionContext context) {
        log.debug("Executing custom step: {}", step.getName());
    }
    
    public static class WorkflowExecutionException extends RuntimeException {
        public WorkflowExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
