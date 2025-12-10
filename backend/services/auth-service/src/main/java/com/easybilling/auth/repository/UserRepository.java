package com.easybilling.auth.repository;

import com.easybilling.auth.entity.User;
import com.easybilling.auth.entity.User.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameAndTenantId(String username, String tenantId);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByTenantId(String tenantId);
    
    List<User> findByStatus(UserStatus status);
    
    List<User> findByTenantIdAndStatus(String tenantId, UserStatus status);
}
