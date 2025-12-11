package com.runtime.engine.controller;

import com.runtime.engine.entity.EntityDefinition;
import com.runtime.engine.entity.FieldDefinition;
import com.runtime.engine.repo.EntityDefinitionRepository;
import com.runtime.engine.repo.FieldDefinitionRepository;
import com.runtime.engine.repo.WorkflowRepository;
import com.runtime.engine.workflow.WorkflowDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class MetadataController {
    
    private final EntityDefinitionRepository entityDefinitionRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final WorkflowRepository workflowRepository;
    
    @GetMapping("/form/{entityName}")
    public ResponseEntity<?> getFormMetadata(
            @PathVariable String entityName,
            @RequestHeader(value = "X-Tenant-Id", required = false, defaultValue = "default") String tenantId) {
        
        try {
            Optional<EntityDefinition> entityDefOpt = entityDefinitionRepository
                    .findByNameAndTenantId(entityName, tenantId);
            
            if (entityDefOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            EntityDefinition entityDef = entityDefOpt.get();
            List<FieldDefinition> fields = fieldDefinitionRepository
                    .findByEntityDefinitionIdOrderByOrderIndexAsc(entityDef.getId());
            
            Map<String, Object> formMetadata = new HashMap<>();
            formMetadata.put("id", entityName + ".form.basic");
            formMetadata.put("entity", entityName);
            formMetadata.put("title", capitalize(entityName));
            
            // Layout
            Map<String, Object> layout = new HashMap<>();
            layout.put("type", "two-column");
            List<Map<String, Object>> areas = new ArrayList<>();
            areas.add(Map.of("name", "left", "width", 60));
            areas.add(Map.of("name", "right", "width", 40));
            layout.put("areas", areas);
            formMetadata.put("layout", layout);
            
            // Fields
            List<Map<String, Object>> fieldMetadata = fields.stream().map(field -> {
                Map<String, Object> fieldMap = new HashMap<>();
                fieldMap.put("name", field.getName());
                fieldMap.put("label", field.getLabel());
                fieldMap.put("component", mapFieldTypeToComponent(field.getDataType()));
                fieldMap.put("required", field.getRequired());
                
                // Validation
                if (field.getValidationRules() != null && !field.getValidationRules().isEmpty()) {
                    fieldMap.put("validation", field.getValidationRules());
                }
                
                // Options for select fields
                if ("SELECT".equalsIgnoreCase(field.getDataType()) && field.getDefaultValue() != null) {
                    fieldMap.put("options", parseOptions(field.getDefaultValue()));
                }
                
                return fieldMap;
            }).collect(Collectors.toList());
            
            formMetadata.put("fields", fieldMetadata);
            
            // Actions
            List<Map<String, String>> actions = new ArrayList<>();
            actions.add(Map.of(
                "id", "save",
                "label", "Save",
                "type", "submit",
                "permission", "entity:" + entityName + ":write"
            ));
            formMetadata.put("actions", actions);
            
            return ResponseEntity.ok(formMetadata);
            
        } catch (Exception e) {
            log.error("Error fetching form metadata for entity: {}", entityName, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/list/{entityName}")
    public ResponseEntity<?> getListMetadata(
            @PathVariable String entityName,
            @RequestHeader(value = "X-Tenant-Id", required = false, defaultValue = "default") String tenantId) {
        
        try {
            Optional<EntityDefinition> entityDefOpt = entityDefinitionRepository
                    .findByNameAndTenantId(entityName, tenantId);
            
            if (entityDefOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            EntityDefinition entityDef = entityDefOpt.get();
            List<FieldDefinition> fields = fieldDefinitionRepository
                    .findByEntityDefinitionIdOrderByOrderIndexAsc(entityDef.getId());
            
            Map<String, Object> listMetadata = new HashMap<>();
            listMetadata.put("id", entityName + ".list");
            listMetadata.put("entity", entityName);
            
            // Columns - show non-JSON fields
            List<Map<String, Object>> columns = fields.stream()
                    .filter(f -> !"JSON".equalsIgnoreCase(f.getDataType()))
                    .limit(6)
                    .map(field -> {
                        Map<String, Object> col = new HashMap<>();
                        col.put("field", field.getName());
                        col.put("label", field.getLabel());
                        col.put("sortable", true);
                        
                        if ("DECIMAL".equalsIgnoreCase(field.getDataType())) {
                            col.put("format", "currency");
                        } else if ("DATE".equalsIgnoreCase(field.getDataType())) {
                            col.put("format", "date");
                        } else if ("DATETIME".equalsIgnoreCase(field.getDataType())) {
                            col.put("format", "datetime");
                        }
                        
                        return col;
                    })
                    .collect(Collectors.toList());
            
            listMetadata.put("columns", columns);
            listMetadata.put("pageSize", 20);
            
            // Row actions
            List<Map<String, String>> rowActions = new ArrayList<>();
            rowActions.add(Map.of(
                "id", "edit",
                "label", "Edit",
                "type", "navigate",
                "target", "/entity/" + entityName + "/edit/{id}"
            ));
            rowActions.add(Map.of(
                "id", "delete",
                "label", "Delete",
                "type", "action",
                "target", "/api/" + entityName + "/{id}"
            ));
            listMetadata.put("rowActions", rowActions);
            
            return ResponseEntity.ok(listMetadata);
            
        } catch (Exception e) {
            log.error("Error fetching list metadata for entity: {}", entityName, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/page/{pageId}")
    public ResponseEntity<?> getPageMetadata(
            @PathVariable String pageId,
            @RequestHeader(value = "X-Tenant-Id", required = false, defaultValue = "default") String tenantId) {
        
        // Return a default page metadata
        Map<String, Object> pageMetadata = new HashMap<>();
        pageMetadata.put("id", pageId);
        pageMetadata.put("title", "Dashboard");
        
        Map<String, Object> layout = new HashMap<>();
        layout.put("regions", Arrays.asList("MAIN"));
        
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(Map.of(
            "region", "MAIN",
            "widget", "KPICard",
            "props", Map.of("title", "Total Revenue", "value", "$125,430", "trend", "+12%")
        ));
        layout.put("items", items);
        
        pageMetadata.put("layout", layout);
        pageMetadata.put("permissions", Arrays.asList("feature:dashboard:view"));
        
        return ResponseEntity.ok(pageMetadata);
    }
    
    @GetMapping("/workflow/{workflowId}")
    public ResponseEntity<?> getWorkflowMetadata(
            @PathVariable String workflowId,
            @RequestHeader(value = "X-Tenant-Id", required = false, defaultValue = "default") String tenantId) {
        
        try {
            Optional<WorkflowDefinition> workflowOpt = workflowRepository
                    .findByNameAndTenantId(workflowId, tenantId);
            
            if (workflowOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            WorkflowDefinition workflow = workflowOpt.get();
            
            Map<String, Object> workflowMetadata = new HashMap<>();
            workflowMetadata.put("id", workflow.getName());
            workflowMetadata.put("name", workflow.getName());
            workflowMetadata.put("steps", workflow.getSteps());
            
            return ResponseEntity.ok(workflowMetadata);
            
        } catch (Exception e) {
            log.error("Error fetching workflow metadata: {}", workflowId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/entities")
    public ResponseEntity<?> getAllEntities(
            @RequestHeader(value = "X-Tenant-Id", required = false, defaultValue = "default") String tenantId) {
        
        try {
            List<EntityDefinition> entities = entityDefinitionRepository.findByTenantId(tenantId);
            
            List<Map<String, Object>> entityList = entities.stream().map(entity -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", entity.getName());
                map.put("label", capitalize(entity.getName()));
                map.put("tableName", entity.getTableName());
                return map;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(entityList);
            
        } catch (Exception e) {
            log.error("Error fetching entities", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    private String mapFieldTypeToComponent(String fieldType) {
        return switch (fieldType.toUpperCase()) {
            case "TEXT", "STRING", "VARCHAR" -> "Text";
            case "TEXTAREA" -> "TextArea";
            case "INTEGER", "LONG", "DECIMAL", "NUMBER" -> "Number";
            case "SELECT", "DROPDOWN" -> "Select";
            case "BOOLEAN", "CHECKBOX" -> "Checkbox";
            case "DATE" -> "Date";
            case "DATETIME" -> "DateTime";
            default -> "Text";
        };
    }
    
    private List<Map<String, String>> parseOptions(String defaultValue) {
        // Simple comma-separated parsing
        if (defaultValue == null || defaultValue.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        String[] options = defaultValue.split(",");
        return Arrays.stream(options)
                .map(String::trim)
                .map(opt -> Map.of("value", opt, "label", opt))
                .collect(Collectors.toList());
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
