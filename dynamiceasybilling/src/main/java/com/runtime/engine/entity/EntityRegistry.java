package com.runtime.engine.entity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Component
@Slf4j
public class EntityRegistry {
    
    private final Map<String, EntityDefinition> entityDefinitions = new ConcurrentHashMap<>();
    private final Map<String, Map<String, FieldDefinition>> entityFields = new ConcurrentHashMap<>();
    
    public void registerEntity(EntityDefinition definition) {
        if (definition == null || definition.getName() == null) {
            throw new IllegalArgumentException("Entity definition and name cannot be null");
        }
        
        entityDefinitions.put(definition.getName(), definition);
        
        Map<String, FieldDefinition> fields = new ConcurrentHashMap<>();
        if (definition.getFields() != null) {
            for (FieldDefinition field : definition.getFields()) {
                fields.put(field.getName(), field);
            }
        }
        entityFields.put(definition.getName(), fields);
        
        log.info("Registered entity: {} with {} fields", definition.getName(), fields.size());
    }
    
    public void unregisterEntity(String entityName) {
        entityDefinitions.remove(entityName);
        entityFields.remove(entityName);
        log.info("Unregistered entity: {}", entityName);
    }
    
    public Optional<EntityDefinition> getEntityDefinition(String entityName) {
        return Optional.ofNullable(entityDefinitions.get(entityName));
    }
    
    public Optional<FieldDefinition> getFieldDefinition(String entityName, String fieldName) {
        Map<String, FieldDefinition> fields = entityFields.get(entityName);
        if (fields != null) {
            return Optional.ofNullable(fields.get(fieldName));
        }
        return Optional.empty();
    }
    
    public List<FieldDefinition> getEntityFields(String entityName) {
        Map<String, FieldDefinition> fields = entityFields.get(entityName);
        return fields != null ? new ArrayList<>(fields.values()) : new ArrayList<>();
    }
    
    public boolean isEntityRegistered(String entityName) {
        return entityDefinitions.containsKey(entityName);
    }
    
    public List<String> getAllEntityNames() {
        return new ArrayList<>(entityDefinitions.keySet());
    }
    
    public List<EntityDefinition> getAllEntityDefinitions() {
        return new ArrayList<>(entityDefinitions.values());
    }
    
    public void clear() {
        entityDefinitions.clear();
        entityFields.clear();
        log.info("Entity registry cleared");
    }
    
    public int getEntityCount() {
        return entityDefinitions.size();
    }
    
    public void reloadEntity(EntityDefinition definition) {
        if (definition != null && definition.getName() != null) {
            unregisterEntity(definition.getName());
            registerEntity(definition);
        }
    }
}
