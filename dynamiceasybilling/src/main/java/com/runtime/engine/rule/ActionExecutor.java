package com.runtime.engine.rule;

import com.runtime.engine.pipeline.RuntimeExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ActionExecutor {
    
    public void execute(Map<String, Object> actions, Map<String, Object> data, RuntimeExecutionContext context) {
        if (actions == null || actions.isEmpty()) {
            return;
        }
        
        log.debug("Executing {} actions", actions.size());
        
        for (Map.Entry<String, Object> entry : actions.entrySet()) {
            String actionType = entry.getKey();
            Object actionConfig = entry.getValue();
            
            try {
                executeAction(actionType, actionConfig, data, context);
            } catch (Exception e) {
                log.error("Error executing action: {}", actionType, e);
            }
        }
    }
    
    private void executeAction(String actionType, Object actionConfig, Map<String, Object> data, RuntimeExecutionContext context) {
        log.debug("Executing action: {}", actionType);
        
        switch (actionType.toLowerCase()) {
            case "set_variable" -> executeSetVariable(actionConfig, data, context);
            case "set_field" -> executeSetField(actionConfig, data, context);
            case "validate" -> executeValidate(actionConfig, data, context);
            case "transform" -> executeTransform(actionConfig, data, context);
            case "log" -> executeLog(actionConfig, data, context);
            case "metadata" -> executeMetadata(actionConfig, data, context);
            default -> log.warn("Unknown action type: {}", actionType);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void executeSetVariable(Object actionConfig, Map<String, Object> data, RuntimeExecutionContext context) {
        if (actionConfig instanceof Map) {
            Map<String, Object> config = (Map<String, Object>) actionConfig;
            String varName = (String) config.get("name");
            Object varValue = config.get("value");
            
            if (varName != null) {
                context.setVariable(varName, varValue);
                log.debug("Set variable: {} = {}", varName, varValue);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void executeSetField(Object actionConfig, Map<String, Object> data, RuntimeExecutionContext context) {
        if (actionConfig instanceof Map) {
            Map<String, Object> config = (Map<String, Object>) actionConfig;
            String fieldName = (String) config.get("field");
            Object fieldValue = config.get("value");
            
            if (fieldName != null && data != null) {
                data.put(fieldName, fieldValue);
                log.debug("Set field: {} = {}", fieldName, fieldValue);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void executeValidate(Object actionConfig, Map<String, Object> data, RuntimeExecutionContext context) {
        if (actionConfig instanceof Map) {
            Map<String, Object> config = (Map<String, Object>) actionConfig;
            String field = (String) config.get("field");
            String message = (String) config.get("message");
            
            if (field != null && message != null) {
                context.addValidationError(field, message);
                log.debug("Added validation error for field: {}", field);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void executeTransform(Object actionConfig, Map<String, Object> data, RuntimeExecutionContext context) {
        if (actionConfig instanceof Map) {
            Map<String, Object> config = (Map<String, Object>) actionConfig;
            String field = (String) config.get("field");
            String operation = (String) config.get("operation");
            
            if (field != null && data != null && data.containsKey(field)) {
                Object value = data.get(field);
                Object transformed = applyTransformation(value, operation);
                data.put(field, transformed);
                log.debug("Transformed field: {} using operation: {}", field, operation);
            }
        }
    }
    
    private Object applyTransformation(Object value, String operation) {
        if (operation == null || value == null) {
            return value;
        }
        
        return switch (operation.toLowerCase()) {
            case "uppercase" -> value.toString().toUpperCase();
            case "lowercase" -> value.toString().toLowerCase();
            case "trim" -> value.toString().trim();
            default -> value;
        };
    }
    
    @SuppressWarnings("unchecked")
    private void executeLog(Object actionConfig, Map<String, Object> data, RuntimeExecutionContext context) {
        if (actionConfig instanceof Map) {
            Map<String, Object> config = (Map<String, Object>) actionConfig;
            String message = (String) config.get("message");
            String level = (String) config.getOrDefault("level", "info");
            
            if (message != null) {
                switch (level.toLowerCase()) {
                    case "debug" -> log.debug(message);
                    case "warn" -> log.warn(message);
                    case "error" -> log.error(message);
                    default -> log.info(message);
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void executeMetadata(Object actionConfig, Map<String, Object> data, RuntimeExecutionContext context) {
        if (actionConfig instanceof Map) {
            Map<String, Object> config = (Map<String, Object>) actionConfig;
            String key = (String) config.get("key");
            Object value = config.get("value");
            
            if (key != null) {
                context.setMetadata(key, value);
                log.debug("Set metadata: {} = {}", key, value);
            }
        }
    }
}
