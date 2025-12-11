package com.easybilling.repository;

import com.easybilling.entity.SecurityGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SecurityGroup entity operations.
 */
@Repository
public interface SecurityGroupRepository extends JpaRepository<SecurityGroup, String> {
    
    /**
     * Find all security groups by tenant ID.
     */
    List<SecurityGroup> findByTenantId(Integer tenantId);
    
    /**
     * Find active security groups by tenant ID.
     */
    List<SecurityGroup> findByTenantIdAndIsActiveTrue(Integer tenantId);
    
    /**
     * Find security group by name and tenant ID.
     */
    Optional<SecurityGroup> findByNameAndTenantId(String name, Integer tenantId);
    
    /**
     * Check if security group exists by name and tenant ID.
     */
    boolean existsByNameAndTenantId(String name, Integer tenantId);
    
    /**
     * Find security groups for a user.
     */
    @Query("SELECT sg FROM SecurityGroup sg " +
           "JOIN UserSecurityGroup usg ON sg.id = usg.securityGroupId " +
           "WHERE usg.userId = :userId AND sg.isActive = true")
    List<SecurityGroup> findByUserId(@Param("userId") String userId);
}
