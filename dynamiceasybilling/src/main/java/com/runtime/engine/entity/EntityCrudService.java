package com.runtime.engine.entity;

import com.runtime.engine.pipeline.RuntimeExecutionContext;
import com.runtime.engine.repo.DynamicEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntityCrudService {
    
    private final DynamicEntityRepository dynamicEntityRepository;
    
    @Transactional
    public DynamicEntity save(DynamicEntity entity, RuntimeExecutionContext context) {
        log.debug("Saving entity of type: {}", entity.getEntityType());
        return dynamicEntityRepository.save(entity);
    }
    
    @Transactional
    public DynamicEntity update(DynamicEntity entity, RuntimeExecutionContext context) {
        log.debug("Updating entity with ID: {}", entity.getId());
        entity.setUpdatedBy(context.getUserId());
        return dynamicEntityRepository.save(entity);
    }
    
    @Transactional(readOnly = true)
    public Optional<DynamicEntity> findById(Long id, RuntimeExecutionContext context) {
        return dynamicEntityRepository.findById(id)
            .filter(entity -> entity.getTenantId().equals(context.getTenantId()));
    }
    
    @Transactional(readOnly = true)
    public List<DynamicEntity> findByEntityType(String entityType, RuntimeExecutionContext context) {
        return dynamicEntityRepository.findByEntityTypeAndTenantId(entityType, context.getTenantId());
    }
    
    @Transactional(readOnly = true)
    public Page<DynamicEntity> findByEntityTypePageable(String entityType, RuntimeExecutionContext context, Pageable pageable) {
        return dynamicEntityRepository.findByEntityTypeAndTenantId(entityType, context.getTenantId(), pageable);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> findByQuery(String entityType, Map<String, Object> queryParams, RuntimeExecutionContext context) {
        int page = queryParams.containsKey("page") ? ((Number) queryParams.get("page")).intValue() : 0;
        int size = queryParams.containsKey("size") ? ((Number) queryParams.get("size")).intValue() : 20;
        
        Pageable pageable = PageRequest.of(page, size);
        Page<DynamicEntity> results = findByEntityTypePageable(entityType, context, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", results.getContent());
        response.put("totalElements", results.getTotalElements());
        response.put("totalPages", results.getTotalPages());
        response.put("currentPage", results.getNumber());
        response.put("pageSize", results.getSize());
        
        return response;
    }
    
    @Transactional
    public void delete(Long id, RuntimeExecutionContext context) {
        DynamicEntity entity = findById(id, context)
            .orElseThrow(() -> new IllegalArgumentException("Entity not found with ID: " + id));
        
        dynamicEntityRepository.delete(entity);
        log.debug("Deleted entity with ID: {}", id);
    }
    
    @Transactional
    public void softDelete(Long id, RuntimeExecutionContext context) {
        DynamicEntity entity = findById(id, context)
            .orElseThrow(() -> new IllegalArgumentException("Entity not found with ID: " + id));
        
        entity.setActive(false);
        entity.setUpdatedBy(context.getUserId());
        dynamicEntityRepository.save(entity);
        log.debug("Soft deleted entity with ID: {}", id);
    }
    
    @Transactional(readOnly = true)
    public List<DynamicEntity> findAll(RuntimeExecutionContext context) {
        return dynamicEntityRepository.findByTenantId(context.getTenantId());
    }
}
