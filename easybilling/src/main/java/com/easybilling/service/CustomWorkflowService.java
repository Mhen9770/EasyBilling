package com.easybilling.service;

import com.easybilling.dto.CustomWorkflowDTO;
import com.easybilling.entity.CustomWorkflow;
import com.easybilling.exception.ResourceNotFoundException;
import com.easybilling.repository.CustomWorkflowRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing custom workflows.
 * Allows tenants to create automated workflows with conditions and actions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomWorkflowService {
    
    private final CustomWorkflowRepository customWorkflowRepository;
    private final ObjectMapper objectMapper;
    
    @Transactional(readOnly = true)
    public List<CustomWorkflowDTO> getAllWorkflows(Integer tenantId) {
        return customWorkflowRepository.findByTenantId(tenantId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CustomWorkflowDTO> getWorkflowsByTrigger(Integer tenantId, String triggerEvent) {
        return customWorkflowRepository.findByTenantIdAndTriggerEventAndIsActiveTrue(tenantId, triggerEvent).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CustomWorkflowDTO> getActiveWorkflows(Integer tenantId) {
        return customWorkflowRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CustomWorkflowDTO getWorkflowById(Long id, Integer tenantId) {
        return customWorkflowRepository.findByIdAndTenantId(id, tenantId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found: " + id));
    }
    
    @Transactional
    public CustomWorkflowDTO createWorkflow(CustomWorkflowDTO dto) {
        CustomWorkflow workflow = CustomWorkflow.builder()
                .tenantId(dto.getTenantId())
                .workflowName(dto.getWorkflowName())
                .description(dto.getDescription())
                .triggerEvent(dto.getTriggerEvent())
                .conditions(dto.getConditions())
                .actions(dto.getActions())
                .executionOrder(dto.getExecutionOrder() != null ? dto.getExecutionOrder() : 0)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .executionCount(0L)
                .failureCount(0L)
                .createdBy(dto.getCreatedBy())
                .build();
        
        workflow = customWorkflowRepository.save(workflow);
        log.info("Created workflow: {} for tenant: {}", workflow.getWorkflowName(), workflow.getTenantId());
        return toDTO(workflow);
    }
    
    @Transactional
    public CustomWorkflowDTO updateWorkflow(Long id, CustomWorkflowDTO dto) {
        CustomWorkflow workflow = customWorkflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found: " + id));
        
        workflow.setWorkflowName(dto.getWorkflowName());
        workflow.setDescription(dto.getDescription());
        workflow.setTriggerEvent(dto.getTriggerEvent());
        workflow.setConditions(dto.getConditions());
        workflow.setActions(dto.getActions());
        workflow.setExecutionOrder(dto.getExecutionOrder());
        workflow.setIsActive(dto.getIsActive());
        
        workflow = customWorkflowRepository.save(workflow);
        log.info("Updated workflow: {}", id);
        return toDTO(workflow);
    }
    
    @Transactional
    public void deleteWorkflow(Long id, Integer tenantId) {
        CustomWorkflow workflow = customWorkflowRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found: " + id));
        
        customWorkflowRepository.delete(workflow);
        log.info("Deleted workflow: {}", id);
    }
    
    @Transactional
    public void toggleWorkflowStatus(Long id, Integer tenantId, Boolean isActive) {
        CustomWorkflow workflow = customWorkflowRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found: " + id));
        
        workflow.setIsActive(isActive);
        customWorkflowRepository.save(workflow);
        log.info("Set workflow {} status to: {}", id, isActive);
    }
    
    /**
     * Execute workflows for a specific trigger event
     * @param triggerEvent The event that triggered the workflow
     * @param eventData The data associated with the event
     * @param tenantId The tenant ID
     */
    @Transactional
    public void executeWorkflows(String triggerEvent, Map<String, Object> eventData, Integer tenantId) {
        List<CustomWorkflow> workflows = customWorkflowRepository
                .findByTenantIdAndTriggerEventAndIsActiveTrue(tenantId, triggerEvent);
        
        // Sort by execution order
        workflows.sort((w1, w2) -> Integer.compare(w1.getExecutionOrder(), w2.getExecutionOrder()));
        
        for (CustomWorkflow workflow : workflows) {
            try {
                if (evaluateConditions(workflow.getConditions(), eventData)) {
                    executeActions(workflow.getActions(), eventData);
                    workflow.setLastExecutedAt(Instant.now());
                    workflow.setExecutionCount(workflow.getExecutionCount() + 1);
                    customWorkflowRepository.save(workflow);
                    log.info("Executed workflow: {} for event: {}", workflow.getWorkflowName(), triggerEvent);
                }
            } catch (Exception e) {
                log.error("Error executing workflow: {}", workflow.getWorkflowName(), e);
                workflow.setFailureCount(workflow.getFailureCount() + 1);
                customWorkflowRepository.save(workflow);
            }
        }
    }
    
    /**
     * Evaluate workflow conditions
     */
    private boolean evaluateConditions(String conditionsJson, Map<String, Object> eventData) {
        if (conditionsJson == null || conditionsJson.isEmpty()) {
            return true; // No conditions means always execute
        }
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> conditions = objectMapper.readValue(conditionsJson, List.class);
            
            for (Map<String, Object> condition : conditions) {
                String field = (String) condition.get("field");
                String operator = (String) condition.get("operator");
                Object expectedValue = condition.get("value");
                Object actualValue = eventData.get(field);
                
                if (!evaluateCondition(actualValue, operator, expectedValue)) {
                    return false; // All conditions must be true
                }
            }
            
            return true;
        } catch (JsonProcessingException e) {
            log.error("Error parsing workflow conditions", e);
            return false;
        }
    }
    
    /**
     * Evaluate a single condition
     */
    private boolean evaluateCondition(Object actualValue, String operator, Object expectedValue) {
        if (actualValue == null) {
            return "IS_NULL".equals(operator);
        }
        
        return switch (operator) {
            case "EQUALS", "==" -> actualValue.equals(expectedValue);
            case "NOT_EQUALS", "!=" -> !actualValue.equals(expectedValue);
            case "GREATER_THAN", ">" -> compareNumeric(actualValue, expectedValue) > 0;
            case "LESS_THAN", "<" -> compareNumeric(actualValue, expectedValue) < 0;
            case "GREATER_THAN_OR_EQUAL", ">=" -> compareNumeric(actualValue, expectedValue) >= 0;
            case "LESS_THAN_OR_EQUAL", "<=" -> compareNumeric(actualValue, expectedValue) <= 0;
            case "CONTAINS" -> actualValue.toString().contains(expectedValue.toString());
            case "STARTS_WITH" -> actualValue.toString().startsWith(expectedValue.toString());
            case "ENDS_WITH" -> actualValue.toString().endsWith(expectedValue.toString());
            case "IS_NULL" -> false; // Already checked above
            case "IS_NOT_NULL" -> true; // Already checked above
            default -> {
                log.warn("Unknown operator: {}", operator);
                yield false;
            }
        };
    }
    
    private int compareNumeric(Object actualValue, Object expectedValue) {
        try {
            double actual = Double.parseDouble(actualValue.toString());
            double expected = Double.parseDouble(expectedValue.toString());
            return Double.compare(actual, expected);
        } catch (NumberFormatException e) {
            log.warn("Cannot compare non-numeric values: {} vs {}", actualValue, expectedValue);
            return 0; // Consider equal if not comparable
        }
    }
    
    /**
     * Execute workflow actions
     */
    private void executeActions(String actionsJson, Map<String, Object> eventData) {
        if (actionsJson == null || actionsJson.isEmpty()) {
            return;
        }
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> actions = objectMapper.readValue(actionsJson, List.class);
            
            for (Map<String, Object> action : actions) {
                String type = (String) action.get("type");
                @SuppressWarnings("unchecked")
                Map<String, Object> config = (Map<String, Object>) action.get("config");
                
                executeAction(type, config, eventData);
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing workflow actions", e);
        }
    }
    
    /**
     * Execute a single action
     * Note: Email, SMS, and Task actions require integration with respective services.
     * These are placeholder implementations that log the intended action.
     */
    private void executeAction(String type, Map<String, Object> config, Map<String, Object> eventData) {
        switch (type) {
            case "SEND_EMAIL" -> {
                // Email sending requires NotificationService integration
                log.info("Email action triggered - To: {}, Subject: {}", 
                    config.get("to"), config.get("subject"));
            }
            case "SEND_SMS" -> {
                // SMS sending requires NotificationService integration
                log.info("SMS action triggered - Phone: {}, Message: {}", 
                    config.get("phone"), config.get("message"));
            }
            case "CREATE_TASK" -> {
                // Task creation requires TaskService integration
                log.info("Task creation action triggered - Title: {}, Assignee: {}", 
                    config.get("title"), config.get("assignee"));
            }
            case "UPDATE_RECORD" -> {
                // Record update requires generic entity service
                log.info("Record update action triggered - Entity: {}, ID: {}", 
                    config.get("entityType"), config.get("entityId"));
            }
            case "CALL_WEBHOOK" -> {
                // Webhook call requires WebhookService integration
                log.info("Webhook action triggered - URL: {}", config.get("url"));
            }
            case "LOG_MESSAGE" -> {
                log.info("Workflow log: {}", config.get("message"));
            }
            default -> log.warn("Unknown action type: {}", type);
        }
    }
    
    private CustomWorkflowDTO toDTO(CustomWorkflow entity) {
        return CustomWorkflowDTO.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .workflowName(entity.getWorkflowName())
                .description(entity.getDescription())
                .triggerEvent(entity.getTriggerEvent())
                .conditions(entity.getConditions())
                .actions(entity.getActions())
                .executionOrder(entity.getExecutionOrder())
                .isActive(entity.getIsActive())
                .lastExecutedAt(entity.getLastExecutedAt())
                .executionCount(entity.getExecutionCount())
                .failureCount(entity.getFailureCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }
}
