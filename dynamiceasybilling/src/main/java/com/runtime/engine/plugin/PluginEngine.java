package com.runtime.engine.plugin;

import com.runtime.engine.pipeline.RuntimeExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PluginEngine {
    
    private final PluginRegistry pluginRegistry;
    
    public void executePlugins(String trigger, Map<String, Object> data, RuntimeExecutionContext context) {
        List<Plugin> plugins = pluginRegistry.getPluginsByTrigger(trigger);
        
        if (plugins.isEmpty()) {
            log.debug("No plugins registered for trigger: {}", trigger);
            return;
        }
        
        log.debug("Executing {} plugins for trigger: {}", plugins.size(), trigger);
        
        for (Plugin plugin : plugins) {
            executePlugin(plugin, data, context);
        }
    }
    
    public void executePlugin(Plugin plugin, Map<String, Object> data, RuntimeExecutionContext context) {
        if (plugin == null || !plugin.isEnabled()) {
            return;
        }
        
        log.debug("Executing plugin: {}", plugin.getName());
        
        try {
            plugin.execute(data, context);
            log.debug("Plugin executed successfully: {}", plugin.getName());
        } catch (Exception e) {
            log.error("Error executing plugin: {}", plugin.getName(), e);
            throw new PluginExecutionException("Failed to execute plugin: " + plugin.getName(), e);
        }
    }
    
    public void registerPlugin(Plugin plugin, Map<String, Object> config) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        
        try {
            plugin.initialize(config);
            pluginRegistry.registerPlugin(plugin);
            log.info("Plugin registered: {}", plugin.getName());
        } catch (Exception e) {
            log.error("Error registering plugin: {}", plugin.getName(), e);
            throw new PluginExecutionException("Failed to register plugin: " + plugin.getName(), e);
        }
    }
    
    public void unregisterPlugin(String pluginName) {
        Plugin plugin = pluginRegistry.getPlugin(pluginName).orElse(null);
        
        if (plugin != null) {
            try {
                plugin.destroy();
                pluginRegistry.unregisterPlugin(pluginName);
                log.info("Plugin unregistered: {}", pluginName);
            } catch (Exception e) {
                log.error("Error unregistering plugin: {}", pluginName, e);
            }
        }
    }
    
    public static class PluginExecutionException extends RuntimeException {
        public PluginExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
