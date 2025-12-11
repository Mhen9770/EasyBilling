package com.runtime.engine.rule;

import com.runtime.engine.pipeline.RuntimeExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RuleEngine {
    
    private final ConditionEvaluator conditionEvaluator;
    private final ActionExecutor actionExecutor;
    
    public void executeRules(List<RuleDefinition> rules, Map<String, Object> data, RuntimeExecutionContext context) {
        if (rules == null || rules.isEmpty()) {
            return;
        }
        
        log.debug("Executing {} rules", rules.size());
        
        rules.stream()
            .filter(RuleDefinition::getActive)
            .sorted((r1, r2) -> r2.getPriority().compareTo(r1.getPriority()))
            .forEach(rule -> executeRule(rule, data, context));
    }
    
    public void executeRule(RuleDefinition rule, Map<String, Object> data, RuntimeExecutionContext context) {
        log.debug("Evaluating rule: {}", rule.getName());
        
        try {
            boolean conditionMet = conditionEvaluator.evaluate(rule.getCondition(), data, context);
            
            if (conditionMet) {
                log.debug("Condition met for rule: {}, executing actions", rule.getName());
                actionExecutor.execute(rule.getActions(), data, context);
            } else {
                log.debug("Condition not met for rule: {}", rule.getName());
            }
        } catch (Exception e) {
            log.error("Error executing rule: {}", rule.getName(), e);
            throw new RuleExecutionException("Failed to execute rule: " + rule.getName(), e);
        }
    }
    
    public boolean evaluateCondition(RuleDefinition rule, Map<String, Object> data, RuntimeExecutionContext context) {
        return conditionEvaluator.evaluate(rule.getCondition(), data, context);
    }
    
    public static class RuleExecutionException extends RuntimeException {
        public RuleExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
