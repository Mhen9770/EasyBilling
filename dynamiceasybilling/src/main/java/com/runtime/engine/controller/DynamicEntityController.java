package com.runtime.engine.controller;

import com.runtime.engine.pipeline.RuntimeExecutionContext;
import com.runtime.engine.pipeline.RuntimePipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/{entity}")
@RequiredArgsConstructor
@Slf4j
public class DynamicEntityController {
    
    private final RuntimePipeline runtimePipeline;
    
    @PostMapping("/create")
    public ResponseEntity<?> create(
            @PathVariable String entity,
            @RequestBody Map<String, Object> data,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        RuntimeExecutionContext context = buildContext(tenantId, userId);
        
        try {
            Object result = runtimePipeline.execute("create", entity, data, context);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error creating entity: {}", entity, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/update")
    public ResponseEntity<?> update(
            @PathVariable String entity,
            @RequestBody Map<String, Object> data,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        RuntimeExecutionContext context = buildContext(tenantId, userId);
        
        try {
            Object result = runtimePipeline.execute("update", entity, data, context);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error updating entity: {}", entity, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/find")
    public ResponseEntity<?> find(
            @PathVariable String entity,
            @RequestParam Map<String, Object> params,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        RuntimeExecutionContext context = buildContext(tenantId, userId);
        
        try {
            Object result = runtimePipeline.execute("find", entity, params, context);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error finding entity: {}", entity, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(
            @PathVariable String entity,
            @PathVariable Long id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        RuntimeExecutionContext context = buildContext(tenantId, userId);
        context.setCurrentEntityId(id);
        
        try {
            Object result = runtimePipeline.execute("find", entity, Map.of("id", id), context);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error finding entity by id: {}", entity, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable String entity,
            @PathVariable Long id,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        RuntimeExecutionContext context = buildContext(tenantId, userId);
        context.setCurrentEntityId(id);
        
        try {
            Object result = runtimePipeline.execute("delete", entity, Map.of("id", id), context);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error deleting entity: {}", entity, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/action/{actionName}")
    public ResponseEntity<?> executeAction(
            @PathVariable String entity,
            @PathVariable String actionName,
            @RequestBody(required = false) Map<String, Object> data,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        RuntimeExecutionContext context = buildContext(tenantId, userId);
        
        try {
            Object result = runtimePipeline.execute(actionName, entity, data != null ? data : Map.of(), context);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error executing action: {} on entity: {}", actionName, entity, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    private RuntimeExecutionContext buildContext(String tenantId, String userId) {
        return RuntimeExecutionContext.builder()
            .tenantId(tenantId != null ? tenantId : "default")
            .userId(userId != null ? userId : "system")
            .requestId(UUID.randomUUID().toString())
            .sessionId(UUID.randomUUID().toString())
            .build();
    }
}
