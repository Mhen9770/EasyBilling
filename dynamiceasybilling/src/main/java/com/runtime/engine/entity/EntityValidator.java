package com.runtime.engine.entity;

import com.runtime.engine.pipeline.RuntimeExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Component
@Slf4j
public class EntityValidator {
    
    public void validate(EntityDefinition definition, Map<String, Object> attributes, RuntimeExecutionContext context) {
        log.debug("Validating entity: {}", definition.getName());
        
        if (definition.getFields() == null || definition.getFields().isEmpty()) {
            return;
        }
        
        for (FieldDefinition field : definition.getFields()) {
            validateField(field, attributes, context);
        }
        
        if (context.isValidationFailed()) {
            throw new ValidationException("Validation failed", context.getValidationErrors());
        }
    }
    
    private void validateField(FieldDefinition field, Map<String, Object> attributes, RuntimeExecutionContext context) {
        Object value = attributes.get(field.getName());
        
        if (field.getRequired() && (value == null || value.toString().trim().isEmpty())) {
            context.addValidationError(field.getName(), "Field is required");
            return;
        }
        
        if (value == null) {
            return;
        }
        
        validateDataType(field, value, context);
        validateLength(field, value, context);
        validatePattern(field, value, context);
        validateCustomRules(field, value, context);
    }
    
    private void validateDataType(FieldDefinition field, Object value, RuntimeExecutionContext context) {
        String dataType = field.getDataType();
        
        try {
            switch (dataType.toLowerCase()) {
                case "string", "text" -> {
                    if (!(value instanceof String)) {
                        context.addValidationError(field.getName(), "Invalid data type, expected String");
                    }
                }
                case "integer", "int" -> {
                    if (!(value instanceof Number)) {
                        context.addValidationError(field.getName(), "Invalid data type, expected Integer");
                    }
                }
                case "decimal", "double", "float" -> {
                    if (!(value instanceof Number)) {
                        context.addValidationError(field.getName(), "Invalid data type, expected Decimal");
                    }
                }
                case "boolean" -> {
                    if (!(value instanceof Boolean)) {
                        context.addValidationError(field.getName(), "Invalid data type, expected Boolean");
                    }
                }
                case "date", "datetime", "timestamp" -> {
                }
                default -> log.debug("Unknown data type: {}", dataType);
            }
        } catch (Exception e) {
            log.error("Error validating data type for field: {}", field.getName(), e);
            context.addValidationError(field.getName(), "Data type validation error");
        }
    }
    
    private void validateLength(FieldDefinition field, Object value, RuntimeExecutionContext context) {
        if (!(value instanceof String)) {
            return;
        }
        
        String strValue = (String) value;
        
        if (field.getMinLength() != null && strValue.length() < field.getMinLength()) {
            context.addValidationError(field.getName(), 
                "Minimum length is " + field.getMinLength());
        }
        
        if (field.getMaxLength() != null && strValue.length() > field.getMaxLength()) {
            context.addValidationError(field.getName(), 
                "Maximum length is " + field.getMaxLength());
        }
    }
    
    private void validatePattern(FieldDefinition field, Object value, RuntimeExecutionContext context) {
        if (field.getPattern() == null || !(value instanceof String)) {
            return;
        }
        
        String strValue = (String) value;
        
        try {
            Pattern pattern = Pattern.compile(field.getPattern());
            if (!pattern.matcher(strValue).matches()) {
                context.addValidationError(field.getName(), 
                    "Value does not match required pattern");
            }
        } catch (Exception e) {
            log.error("Error validating pattern for field: {}", field.getName(), e);
            context.addValidationError(field.getName(), "Pattern validation error");
        }
    }
    
    private void validateCustomRules(FieldDefinition field, Object value, RuntimeExecutionContext context) {
        Map<String, Object> validationRules = field.getValidationRules();
        
        if (validationRules == null || validationRules.isEmpty()) {
            return;
        }
        
        if (validationRules.containsKey("min") && value instanceof Number) {
            Number min = (Number) validationRules.get("min");
            if (((Number) value).doubleValue() < min.doubleValue()) {
                context.addValidationError(field.getName(), "Value must be at least " + min);
            }
        }
        
        if (validationRules.containsKey("max") && value instanceof Number) {
            Number max = (Number) validationRules.get("max");
            if (((Number) value).doubleValue() > max.doubleValue()) {
                context.addValidationError(field.getName(), "Value must be at most " + max);
            }
        }
    }
    
    public static class ValidationException extends RuntimeException {
        private final Map<String, Object> errors;
        
        public ValidationException(String message, Map<String, Object> errors) {
            super(message);
            this.errors = errors;
        }
        
        public Map<String, Object> getErrors() {
            return errors;
        }
    }
}
