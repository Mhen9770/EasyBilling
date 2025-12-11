package com.easybilling.repository;

import com.easybilling.entity.User;
import com.easybilling.entity.User.UserStatus;
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
    
    Optional<User> findByUsernameAndTenantId(String username, Integer tenantId);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByTenantId(Integer tenantId);
    
    List<User> findByStatus(UserStatus status);
    
    List<User> findByTenantIdAndStatus(Integer tenantId, UserStatus status);
}
