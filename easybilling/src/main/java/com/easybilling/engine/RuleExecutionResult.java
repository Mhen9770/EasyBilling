package com.easybilling.engine;

import com.easybilling.entity.rule.BusinessRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of rule execution.
 * Contains information about which rules were executed and their outcomes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionResult {
    
    /**
     * Rules that were successfully executed.
     */
    @Builder.Default
    private List<ExecutedRuleInfo> executedRules = new ArrayList<>();
    
    /**
     * Rules that were skipped (conditions not met).
     */
    @Builder.Default
    private List<SkippedRuleInfo> skippedRules = new ArrayList<>();
    
    /**
     * Rules that failed during execution.
     */
    @Builder.Default
    private List<FailedRuleInfo> failedRules = new ArrayList<>();
    
    /**
     * Aggregated results from all executed rules.
     */
    @Builder.Default
    private Map<String, Object> aggregatedResults = new HashMap<>();
    
    /**
     * Add an executed rule.
     */
    public void addExecutedRule(BusinessRule rule, RuleActionResult actionResult) {
        executedRules.add(new ExecutedRuleInfo(
            rule.getId(),
            rule.getRuleName(),
            actionResult
        ));
        
        // Aggregate results
        if (actionResult.getResults() != null) {
            aggregatedResults.putAll(actionResult.getResults());
        }
    }
    
    /**
     * Add a skipped rule.
     */
    public void addSkippedRule(BusinessRule rule) {
        skippedRules.add(new SkippedRuleInfo(
            rule.getId(),
            rule.getRuleName(),
            "Conditions not met"
        ));
    }
    
    /**
     * Add a failed rule.
     */
    public void addFailedRule(BusinessRule rule, String errorMessage) {
        failedRules.add(new FailedRuleInfo(
            rule.getId(),
            rule.getRuleName(),
            errorMessage
        ));
    }
    
    /**
     * Check if any rules were executed.
     */
    public boolean hasExecutedRules() {
        return !executedRules.isEmpty();
    }
    
    /**
     * Check if any rules failed.
     */
    public boolean hasFailures() {
        return !failedRules.isEmpty();
    }
    
    /**
     * Get a specific result value.
     */
    public Object getResult(String key) {
        return aggregatedResults.get(key);
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExecutedRuleInfo {
        private String ruleId;
        private String ruleName;
        private RuleActionResult actionResult;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkippedRuleInfo {
        private String ruleId;
        private String ruleName;
        private String reason;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FailedRuleInfo {
        private String ruleId;
        private String ruleName;
        private String errorMessage;
    }
}
