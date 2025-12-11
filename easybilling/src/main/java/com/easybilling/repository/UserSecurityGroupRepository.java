package com.easybilling.repository;

import com.easybilling.entity.UserSecurityGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserSecurityGroup entity operations.
 */
@Repository
public interface UserSecurityGroupRepository extends JpaRepository<UserSecurityGroup, String> {
    
    /**
     * Find all security group assignments for a user.
     */
    List<UserSecurityGroup> findByUserId(String userId);
    
    /**
     * Find all users in a security group.
     */
    List<UserSecurityGroup> findBySecurityGroupId(String securityGroupId);
    
    /**
     * Count users in a security group.
     */
    long countBySecurityGroupId(String securityGroupId);
    
    /**
     * Check if user is assigned to a security group.
     */
    boolean existsByUserIdAndSecurityGroupId(String userId, String securityGroupId);
    
    /**
     * Find specific user-security group assignment.
     */
    Optional<UserSecurityGroup> findByUserIdAndSecurityGroupId(String userId, String securityGroupId);
    
    /**
     * Delete all security group assignments for a user.
     */
    @Modifying
    @Query("DELETE FROM UserSecurityGroup usg WHERE usg.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
    
    /**
     * Delete all user assignments for a security group.
     */
    @Modifying
    @Query("DELETE FROM UserSecurityGroup usg WHERE usg.securityGroupId = :securityGroupId")
    void deleteBySecurityGroupId(@Param("securityGroupId") String securityGroupId);
    
    /**
     * Delete specific user-security group assignment.
     */
    void deleteByUserIdAndSecurityGroupId(String userId, String securityGroupId);
}
