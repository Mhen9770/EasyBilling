package com.easybilling.repository.rule;

import com.easybilling.entity.rule.BusinessRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for BusinessRule entity.
 */
@Repository
public interface BusinessRuleRepository extends JpaRepository<BusinessRule, String> {
    
    /**
     * Find all active rules for a specific type.
     */
    List<BusinessRule> findByTenantIdAndRuleTypeAndIsActive(
        Integer tenantId, String ruleType, Boolean isActive
    );
    
    /**
     * Find rule by name.
     */
    Optional<BusinessRule> findByTenantIdAndRuleName(Integer tenantId, String ruleName);
    
    /**
     * Find all rules in a category.
     */
    List<BusinessRule> findByTenantIdAndCategory(Integer tenantId, String category);
    
    /**
     * Find currently valid rules (active and within date range).
     */
    @Query("""
        SELECT r FROM BusinessRule r 
        WHERE r.tenantId = :tenantId 
        AND r.ruleType = :ruleType 
        AND r.isActive = true
        AND (r.startDate IS NULL OR r.startDate <= :now)
        AND (r.endDate IS NULL OR r.endDate >= :now)
        ORDER BY r.priority DESC
    """)
    List<BusinessRule> findValidRules(
        @Param("tenantId") Integer tenantId,
        @Param("ruleType") String ruleType,
        @Param("now") Instant now
    );
}
