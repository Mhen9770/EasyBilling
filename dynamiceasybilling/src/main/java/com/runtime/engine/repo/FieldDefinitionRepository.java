package com.runtime.engine.repo;

import com.runtime.engine.entity.FieldDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldDefinitionRepository extends JpaRepository<FieldDefinition, Long> {
    
    List<FieldDefinition> findByEntityDefinitionId(Long entityDefinitionId);
    
    List<FieldDefinition> findByEntityDefinitionIdOrderByOrderIndexAsc(Long entityDefinitionId);
}
