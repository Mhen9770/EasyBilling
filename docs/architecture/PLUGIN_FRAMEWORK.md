# Plugin Framework Architecture

## Overview

The Plugin Framework enables extensibility through a modular add-on system. Tenants can enable/disable plugins for integrations like Tally Export, WhatsApp messaging, Shopify sync, etc.

## Core Components

### 1. Plugin Interface

```java
package com.easybilling.plugin;

import com.easybilling.engine.RuleContext;

/**
 * Base interface that all plugins must implement.
 */
public interface PluginExecutor {
    
    /**
     * Get plugin code/identifier.
     */
    String getPluginCode();
    
    /**
     * Get plugin name.
     */
    String getPluginName();
    
    /**
     * Get plugin version.
     */
    String getVersion();
    
    /**
     * Initialize the plugin with configuration.
     * Called when plugin is enabled.
     */
    void initialize(Map<String, Object> configuration);
    
    /**
     * Execute the plugin logic.
     * 
     * @param context Execution context with data
     * @return Result of plugin execution
     */
    PluginExecutionResult execute(RuleContext context);
    
    /**
     * Validate plugin configuration.
     * Called before saving configuration.
     */
    boolean validateConfiguration(Map<String, Object> configuration);
    
    /**
     * Cleanup resources when plugin is disabled.
     */
    void shutdown();
}
```

### 2. Example Plugin: Tally Export

```java
package com.easybilling.plugin.impl;

import com.easybilling.engine.RuleContext;
import com.easybilling.plugin.PluginExecutor;
import com.easybilling.plugin.PluginExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Plugin for exporting invoices to Tally accounting software.
 */
@Component
@Slf4j
public class TallyExportPlugin implements PluginExecutor {
    
    private String apiEndpoint;
    private String companyId;
    private boolean isInitialized = false;
    
    @Override
    public String getPluginCode() {
        return "tally_export";
    }
    
    @Override
    public String getPluginName() {
        return "Tally Export Integration";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public void initialize(Map<String, Object> configuration) {
        this.apiEndpoint = (String) configuration.get("apiEndpoint");
        this.companyId = (String) configuration.get("companyId");
        this.isInitialized = true;
        log.info("Tally Export Plugin initialized");
    }
    
    @Override
    public PluginExecutionResult execute(RuleContext context) {
        if (!isInitialized) {
            throw new IllegalStateException("Plugin not initialized");
        }
        
        PluginExecutionResult result = new PluginExecutionResult();
        
        try {
            // Get invoice data from context
            Map<String, Object> invoice = (Map<String, Object>) context.getValue("invoice");
            
            // Convert to Tally XML format
            String tallyXml = convertToTallyXML(invoice);
            
            // Send to Tally API
            boolean sent = sendToTally(tallyXml);
            
            result.setSuccess(sent);
            result.addResult("tallyVoucherNumber", "VCH-" + System.currentTimeMillis());
            result.addMessage("Invoice exported to Tally successfully");
            
        } catch (Exception e) {
            log.error("Error exporting to Tally", e);
            result.setSuccess(false);
            result.addError(e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public boolean validateConfiguration(Map<String, Object> configuration) {
        return configuration.containsKey("apiEndpoint") 
            && configuration.containsKey("companyId");
    }
    
    @Override
    public void shutdown() {
        log.info("Tally Export Plugin shutdown");
        isInitialized = false;
    }
    
    private String convertToTallyXML(Map<String, Object> invoice) {
        // Convert invoice to Tally XML format
        // This is a simplified example
        return "<TALLYMESSAGE>...</TALLYMESSAGE>";
    }
    
    private boolean sendToTally(String xml) {
        // Send XML to Tally API
        // This is a simplified example
        return true;
    }
}
```

### 3. Plugin Service

```java
package com.easybilling.service.plugin;

import com.easybilling.engine.RuleContext;
import com.easybilling.entity.plugin.Plugin;
import com.easybilling.plugin.PluginExecutor;
import com.easybilling.plugin.PluginExecutionResult;
import com.easybilling.repository.plugin.PluginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing and executing plugins.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PluginService {
    
    private final PluginRepository pluginRepository;
    private final Map<String, PluginExecutor> pluginExecutors = new HashMap<>();
    
    /**
     * Register a plugin executor.
     */
    public void registerPlugin(PluginExecutor executor) {
        pluginExecutors.put(executor.getPluginCode(), executor);
        log.info("Registered plugin: {}", executor.getPluginCode());
    }
    
    /**
     * Execute a plugin for a tenant.
     */
    @Transactional
    public PluginExecutionResult executePlugin(Integer tenantId, String pluginCode, 
                                              RuleContext context) {
        // Get plugin configuration
        Plugin plugin = pluginRepository.findByTenantIdAndPluginCode(tenantId, pluginCode)
            .orElseThrow(() -> new RuntimeException("Plugin not found: " + pluginCode));
        
        if (!plugin.getIsEnabled()) {
            throw new IllegalStateException("Plugin is not enabled: " + pluginCode);
        }
        
        // Get plugin executor
        PluginExecutor executor = pluginExecutors.get(pluginCode);
        if (executor == null) {
            throw new RuntimeException("Plugin executor not found: " + pluginCode);
        }
        
        // Initialize if needed
        if (!isInitialized(pluginCode)) {
            executor.initialize(plugin.getConfiguration());
        }
        
        // Execute plugin
        PluginExecutionResult result = executor.execute(context);
        
        // Update execution stats
        plugin.incrementExecutionCount();
        pluginRepository.save(plugin);
        
        return result;
    }
    
    /**
     * Get all enabled plugins for a tenant.
     */
    @Transactional(readOnly = true)
    public List<Plugin> getEnabledPlugins(Integer tenantId) {
        return pluginRepository.findByTenantIdAndIsEnabled(tenantId, true);
    }
    
    /**
     * Enable a plugin for a tenant.
     */
    @Transactional
    public void enablePlugin(Integer tenantId, String pluginCode, 
                            Map<String, Object> configuration) {
        Plugin plugin = pluginRepository.findByTenantIdAndPluginCode(tenantId, pluginCode)
            .orElseThrow(() -> new RuntimeException("Plugin not found: " + pluginCode));
        
        // Validate configuration
        PluginExecutor executor = pluginExecutors.get(pluginCode);
        if (executor != null && !executor.validateConfiguration(configuration)) {
            throw new IllegalArgumentException("Invalid plugin configuration");
        }
        
        plugin.setConfiguration(configuration);
        plugin.setIsEnabled(true);
        pluginRepository.save(plugin);
        
        log.info("Enabled plugin: {} for tenant: {}", pluginCode, tenantId);
    }
    
    /**
     * Disable a plugin for a tenant.
     */
    @Transactional
    public void disablePlugin(Integer tenantId, String pluginCode) {
        Plugin plugin = pluginRepository.findByTenantIdAndPluginCode(tenantId, pluginCode)
            .orElseThrow(() -> new RuntimeException("Plugin not found: " + pluginCode));
        
        plugin.setIsEnabled(false);
        pluginRepository.save(plugin);
        
        // Shutdown plugin executor
        PluginExecutor executor = pluginExecutors.get(pluginCode);
        if (executor != null) {
            executor.shutdown();
        }
        
        log.info("Disabled plugin: {} for tenant: {}", pluginCode, tenantId);
    }
    
    private boolean isInitialized(String pluginCode) {
        // Check if plugin is already initialized
        // This is simplified - actual implementation would track initialization state
        return true;
    }
}
```

## Plugin Configuration Examples

### WhatsApp Integration Plugin

```json
{
  "pluginCode": "whatsapp_integration",
  "pluginName": "WhatsApp Business Integration",
  "pluginType": "notification",
  "category": "communication",
  "configuration": {
    "apiKey": "xxx",
    "phoneNumberId": "1234567890",
    "accessToken": "yyy",
    "webhookUrl": "https://myapp.com/webhook/whatsapp",
    "templates": {
      "invoice_sent": "invoice_notification",
      "payment_reminder": "payment_reminder_template"
    }
  }
}
```

### Shopify Sync Plugin

```json
{
  "pluginCode": "shopify_sync",
  "pluginName": "Shopify E-commerce Sync",
  "pluginType": "integration",
  "category": "ecommerce",
  "configuration": {
    "shopifyDomain": "mystore.myshopify.com",
    "apiKey": "xxx",
    "apiSecret": "yyy",
    "accessToken": "zzz",
    "syncInterval": "hourly",
    "syncProducts": true,
    "syncOrders": true,
    "syncInventory": true,
    "inventoryLocationId": "12345"
  }
}
```

## Best Practices

1. **Stateless Execution**: Plugins should be stateless where possible
2. **Error Handling**: Always handle errors gracefully
3. **Configuration Validation**: Validate all configuration before saving
4. **Logging**: Log all plugin executions for audit trail
5. **Timeout**: Implement timeout for long-running operations
6. **Retry Logic**: Implement retry for transient failures
7. **Security**: Never log sensitive configuration (API keys, secrets)

