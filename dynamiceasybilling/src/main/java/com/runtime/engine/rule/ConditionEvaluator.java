package com.runtime.engine.rule;

import com.runtime.engine.pipeline.RuntimeExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ConditionEvaluator {
    
    private final ExpressionParser parser = new SpelExpressionParser();
    private final Map<String, Expression> compiledExpressions = new ConcurrentHashMap<>();
    
    public boolean evaluate(String condition, Map<String, Object> data, RuntimeExecutionContext context) {
        if (condition == null || condition.trim().isEmpty()) {
            return true;
        }
        
        try {
            Expression expression = getOrCompileExpression(condition);
            StandardEvaluationContext evalContext = createEvaluationContext(data, context);
            
            Object result = expression.getValue(evalContext);
            
            return result instanceof Boolean ? (Boolean) result : false;
        } catch (Exception e) {
            log.error("Error evaluating condition: {}", condition, e);
            return false;
        }
    }
    
    private Expression getOrCompileExpression(String condition) {
        return compiledExpressions.computeIfAbsent(condition, parser::parseExpression);
    }
    
    private StandardEvaluationContext createEvaluationContext(Map<String, Object> data, RuntimeExecutionContext context) {
        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        
        if (data != null) {
            data.forEach(evalContext::setVariable);
        }
        
        evalContext.setVariable("tenantId", context.getTenantId());
        evalContext.setVariable("userId", context.getUserId());
        evalContext.setVariable("context", context);
        
        if (context.getVariables() != null) {
            context.getVariables().forEach(evalContext::setVariable);
        }
        
        return evalContext;
    }
    
    public void clearCache() {
        compiledExpressions.clear();
        log.info("Condition expression cache cleared");
    }
}
