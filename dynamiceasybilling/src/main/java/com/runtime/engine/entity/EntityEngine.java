package com.runtime.engine.entity;

import com.runtime.engine.pipeline.RuntimeExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EntityEngine {
    
    private final EntityRegistry entityRegistry;
    private final EntityValidator entityValidator;
    private final EntityCrudService entityCrudService;
    
    public DynamicEntity createEntity(String entityName, Map<String, Object> attributes, RuntimeExecutionContext context) {
        log.debug("Creating entity: {}", entityName);
        
        EntityDefinition definition = entityRegistry.getEntityDefinition(entityName)
            .orElseThrow(() -> new IllegalArgumentException("Entity definition not found: " + entityName));
        
        if (!definition.getActive()) {
            throw new IllegalStateException("Entity definition is not active: " + entityName);
        }
        
        entityValidator.validate(definition, attributes, context);
        
        DynamicEntity entity = buildEntity(entityName, attributes, context);
        
        return entityCrudService.save(entity, context);
    }
    
    public DynamicEntity updateEntity(Long entityId, String entityName, Map<String, Object> attributes, RuntimeExecutionContext context) {
        log.debug("Updating entity: {} with ID: {}", entityName, entityId);
        
        EntityDefinition definition = entityRegistry.getEntityDefinition(entityName)
            .orElseThrow(() -> new IllegalArgumentException("Entity definition not found: " + entityName));
        
        entityValidator.validate(definition, attributes, context);
        
        DynamicEntity entity = entityCrudService.findById(entityId, context)
            .orElseThrow(() -> new IllegalArgumentException("Entity not found with ID: " + entityId));
        
        if (!entity.getEntityType().equals(entityName)) {
            throw new IllegalArgumentException("Entity type mismatch");
        }
        
        updateEntityAttributes(entity, attributes);
        
        return entityCrudService.update(entity, context);
    }
    
    public DynamicEntity findById(Long entityId, RuntimeExecutionContext context) {
        return entityCrudService.findById(entityId, context)
            .orElseThrow(() -> new IllegalArgumentException("Entity not found with ID: " + entityId));
    }
    
    public void deleteEntity(Long entityId, RuntimeExecutionContext context) {
        log.debug("Deleting entity with ID: {}", entityId);
        entityCrudService.delete(entityId, context);
    }
    
    public Map<String, Object> findByQuery(String entityName, Map<String, Object> queryParams, RuntimeExecutionContext context) {
        EntityDefinition definition = entityRegistry.getEntityDefinition(entityName)
            .orElseThrow(() -> new IllegalArgumentException("Entity definition not found: " + entityName));
        
        return entityCrudService.findByQuery(entityName, queryParams, context);
    }
    
    private DynamicEntity buildEntity(String entityName, Map<String, Object> attributes, RuntimeExecutionContext context) {
        DynamicEntity entity = new DynamicEntity();
        entity.setEntityType(entityName);
        entity.setTenantId(context.getTenantId());
        entity.setAttributes(attributes);
        entity.setCreatedBy(context.getUserId());
        entity.setUpdatedBy(context.getUserId());
        return entity;
    }
    
    private void updateEntityAttributes(DynamicEntity entity, Map<String, Object> attributes) {
        if (attributes != null) {
            entity.setAttributes(attributes);
        }
    }
}
