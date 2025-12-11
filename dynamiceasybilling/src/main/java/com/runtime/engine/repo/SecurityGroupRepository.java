package com.runtime.engine.repo;

import com.runtime.engine.permission.SecurityGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityGroupRepository extends JpaRepository<SecurityGroup, Long> {
    
    Optional<SecurityGroup> findByName(String name);
    
    List<SecurityGroup> findByTenantId(String tenantId);
    
    List<SecurityGroup> findByTenantIdAndActive(String tenantId, Boolean active);
    
    @Query("SELECT sg FROM SecurityGroup sg JOIN sg.members m WHERE m = :userId AND sg.tenantId = :tenantId AND sg.active = true")
    List<SecurityGroup> findByUserIdAndTenantId(@Param("userId") String userId, @Param("tenantId") String tenantId);
}
