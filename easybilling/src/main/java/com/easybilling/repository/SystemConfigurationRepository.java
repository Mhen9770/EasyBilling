package com.easybilling.repository;

import com.easybilling.entity.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Long> {
    
    Optional<SystemConfiguration> findByConfigKey(String configKey);
    
    List<SystemConfiguration> findByCategory(String category);
    
    List<SystemConfiguration> findByIsEditableTrue();
    
    boolean existsByConfigKey(String configKey);
}
