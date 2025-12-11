package com.easybilling.engine;

import com.easybilling.entity.rule.BusinessRule;
import com.easybilling.repository.rule.BusinessRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Core Rule Engine for evaluating and executing business rules.
 * Supports discounts, workflows, inventory management, pricing, and validation rules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEngine {
    
    private final BusinessRuleRepository ruleRepository;
    private final RuleEvaluator ruleEvaluator;
    private final RuleActionExecutor actionExecutor;
    
    /**
     * Evaluate all active rules for a specific type and context.
     * Rules are executed in priority order (highest first).
     * 
     * @param tenantId The tenant ID
     * @param ruleType The type of rules to evaluate (discount, workflow, etc.)
     * @param context The context containing data for rule evaluation
     * @return Result containing executed, skipped, and failed rules
     */
    public RuleExecutionResult evaluateRules(Integer tenantId, String ruleType, 
                                            RuleContext context) {
        log.debug("Evaluating rules: tenant={}, type={}", tenantId, ruleType);
        
        List<BusinessRule> rules = getActiveRules(tenantId, ruleType);
        RuleExecutionResult result = new RuleExecutionResult();
        
        for (BusinessRule rule : rules) {
            try {
                long startTime = System.currentTimeMillis();
                
                // Evaluate rule conditions
                boolean conditionsMet = ruleEvaluator.evaluate(rule.getConditions(), context);
                
                if (conditionsMet) {
                    log.info("Rule conditions met, executing actions: {}", rule.getRuleName());
                    
                    // Execute rule actions
                    RuleActionResult actionResult = actionExecutor.execute(
                        rule.getActions(), 
                        context,
                        rule.getParameters()
                    );
                    
                    result.addExecutedRule(rule, actionResult);
                    
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("Rule executed successfully in {}ms: {}", duration, rule.getRuleName());
                } else {
                    log.debug("Rule conditions not met, skipping: {}", rule.getRuleName());
                    result.addSkippedRule(rule);
                }
                
            } catch (Exception e) {
                log.error("Error evaluating rule: {}", rule.getRuleName(), e);
                result.addFailedRule(rule, e.getMessage());
            }
        }
        
        log.info("Rule evaluation complete: executed={}, skipped={}, failed={}", 
                 result.getExecutedRules().size(),
                 result.getSkippedRules().size(),
                 result.getFailedRules().size());
        
        return result;
    }
    
    /**
     * Get active rules for tenant and type, sorted by priority.
     * Results are cached for performance.
     */
    @Cacheable(value = "businessRules", key = "#tenantId + '_' + #ruleType")
    @Transactional(readOnly = true)
    public List<BusinessRule> getActiveRules(Integer tenantId, String ruleType) {
        log.debug("Loading active rules: tenant={}, type={}", tenantId, ruleType);
        Instant now = Instant.now();
        return ruleRepository.findValidRules(tenantId, ruleType, now);
    }
    
    /**
     * Evaluate a single rule.
     * 
     * @param ruleId The rule ID
     * @param context The context for evaluation
     * @return Result of rule evaluation and execution
     */
    @Transactional(readOnly = true)
    public RuleExecutionResult evaluateSingleRule(String ruleId, RuleContext context) {
        log.debug("Evaluating single rule: {}", ruleId);
        
        BusinessRule rule = ruleRepository.findById(ruleId)
            .orElseThrow(() -> new RuntimeException("Rule not found: " + ruleId));
        
        RuleExecutionResult result = new RuleExecutionResult();
        
        try {
            boolean conditionsMet = ruleEvaluator.evaluate(rule.getConditions(), context);
            
            if (conditionsMet) {
                RuleActionResult actionResult = actionExecutor.execute(
                    rule.getActions(), 
                    context,
                    rule.getParameters()
                );
                result.addExecutedRule(rule, actionResult);
            } else {
                result.addSkippedRule(rule);
            }
        } catch (Exception e) {
            log.error("Error evaluating rule: {}", ruleId, e);
            result.addFailedRule(rule, e.getMessage());
        }
        
        return result;
    }
}
