package com.easybilling.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Evaluates rule conditions against context data.
 * Supports complex boolean logic (AND, OR) and various operators.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RuleEvaluator {
    
    /**
     * Evaluate rule conditions.
     * 
     * @param conditions The conditions to evaluate
     * @param context The context containing data
     * @return true if conditions are met, false otherwise
     */
    @SuppressWarnings("unchecked")
    public boolean evaluate(Map<String, Object> conditions, RuleContext context) {
        if (conditions == null || conditions.isEmpty()) {
            log.debug("No conditions specified, returning true");
            return true;
        }
        
        String operator = (String) conditions.get("operator");
        Object rulesObj = conditions.get("rules");
        
        if (rulesObj instanceof List) {
            List<Map<String, Object>> rules = (List<Map<String, Object>>) rulesObj;
            
            if ("AND".equalsIgnoreCase(operator)) {
                boolean result = rules.stream()
                    .allMatch(rule -> evaluateSingleCondition(rule, context));
                log.debug("AND evaluation result: {}", result);
                return result;
            } else if ("OR".equalsIgnoreCase(operator)) {
                boolean result = rules.stream()
                    .anyMatch(rule -> evaluateSingleCondition(rule, context));
                log.debug("OR evaluation result: {}", result);
                return result;
            }
        }
        
        // Single condition
        return evaluateSingleCondition(conditions, context);
    }
    
    /**
     * Evaluate a single condition.
     */
    @SuppressWarnings("unchecked")
    private boolean evaluateSingleCondition(Map<String, Object> condition, RuleContext context) {
        String field = (String) condition.get("field");
        String operator = (String) condition.get("operator");
        Object expectedValue = condition.get("value");
        String valueField = (String) condition.get("valueField");
        
        // Get actual value from context
        Object actualValue = context.getValue(field);
        
        // If valueField is specified, get expected value from another field
        if (valueField != null) {
            expectedValue = context.getValue(valueField);
        }
        
        log.debug("Evaluating: field={}, operator={}, actual={}, expected={}", 
                 field, operator, actualValue, expectedValue);
        
        boolean result = evaluateOperator(operator, actualValue, expectedValue);
        log.debug("Condition result: {}", result);
        
        return result;
    }
    
    /**
     * Evaluate operator between actual and expected values.
     */
    @SuppressWarnings("unchecked")
    private boolean evaluateOperator(String operator, Object actual, Object expected) {
        if (actual == null) {
            return "isNull".equalsIgnoreCase(operator) || 
                   ("equals".equalsIgnoreCase(operator) && expected == null);
        }
        
        try {
            switch (operator.toLowerCase()) {
                case "equals":
                    return actual.equals(expected);
                    
                case "notequals":
                case "not_equals":
                    return !actual.equals(expected);
                    
                case "greaterthan":
                case "greater_than":
                    return compareNumbers(actual, expected) > 0;
                    
                case "greaterthanorequals":
                case "greater_than_or_equals":
                    return compareNumbers(actual, expected) >= 0;
                    
                case "lessthan":
                case "less_than":
                    return compareNumbers(actual, expected) < 0;
                    
                case "lessthanorequals":
                case "less_than_or_equals":
                    return compareNumbers(actual, expected) <= 0;
                    
                case "in":
                    if (expected instanceof List) {
                        return ((List<?>) expected).contains(actual);
                    }
                    return false;
                    
                case "notin":
                case "not_in":
                    if (expected instanceof List) {
                        return !((List<?>) expected).contains(actual);
                    }
                    return true;
                    
                case "contains":
                    return actual.toString().contains(expected.toString());
                    
                case "notcontains":
                case "not_contains":
                    return !actual.toString().contains(expected.toString());
                    
                case "startswith":
                case "starts_with":
                    return actual.toString().startsWith(expected.toString());
                    
                case "endswith":
                case "ends_with":
                    return actual.toString().endsWith(expected.toString());
                    
                case "isnull":
                case "is_null":
                    return false; // actual is not null if we reached here
                    
                case "isnotnull":
                case "is_not_null":
                    return true; // actual is not null if we reached here
                    
                case "isempty":
                case "is_empty":
                    return actual.toString().isEmpty();
                    
                case "isnotempty":
                case "is_not_empty":
                    return !actual.toString().isEmpty();
                    
                default:
                    log.warn("Unknown operator: {}", operator);
                    return false;
            }
        } catch (Exception e) {
            log.error("Error evaluating operator: {}", operator, e);
            return false;
        }
    }
    
    /**
     * Compare two objects as numbers.
     */
    private int compareNumbers(Object actual, Object expected) {
        try {
            BigDecimal actualNum = toBigDecimal(actual);
            BigDecimal expectedNum = toBigDecimal(expected);
            return actualNum.compareTo(expectedNum);
        } catch (Exception e) {
            log.error("Error comparing numbers: actual={}, expected={}", actual, expected, e);
            throw new IllegalArgumentException("Cannot compare values as numbers", e);
        }
    }
    
    /**
     * Convert object to BigDecimal.
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        } else {
            return new BigDecimal(value.toString());
        }
    }
}
