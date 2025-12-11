package com.runtime.engine.plugin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class PluginRegistry {
    
    private final Map<String, Plugin> plugins = new ConcurrentHashMap<>();
    private final Map<String, List<Plugin>> pluginsByTrigger = new ConcurrentHashMap<>();
    private final Map<String, List<Plugin>> pluginsByType = new ConcurrentHashMap<>();
    
    public void registerPlugin(Plugin plugin) {
        if (plugin == null || plugin.getName() == null) {
            throw new IllegalArgumentException("Plugin and plugin name cannot be null");
        }
        
        plugins.put(plugin.getName(), plugin);
        
        if (plugin instanceof TriggerBasedPlugin) {
            String trigger = ((TriggerBasedPlugin) plugin).getTrigger();
            pluginsByTrigger.computeIfAbsent(trigger, k -> new ArrayList<>())
                .add(plugin);
        }
        
        pluginsByType.computeIfAbsent(plugin.getPluginType(), k -> new ArrayList<>())
            .add(plugin);
        
        log.info("Registered plugin: {} of type: {}", plugin.getName(), plugin.getPluginType());
    }
    
    public void unregisterPlugin(String pluginName) {
        Plugin plugin = plugins.remove(pluginName);
        
        if (plugin != null) {
            if (plugin instanceof TriggerBasedPlugin) {
                String trigger = ((TriggerBasedPlugin) plugin).getTrigger();
                List<Plugin> triggerPlugins = pluginsByTrigger.get(trigger);
                if (triggerPlugins != null) {
                    triggerPlugins.remove(plugin);
                }
            }
            
            List<Plugin> typePlugins = pluginsByType.get(plugin.getPluginType());
            if (typePlugins != null) {
                typePlugins.remove(plugin);
            }
            
            log.info("Unregistered plugin: {}", pluginName);
        }
    }
    
    public Optional<Plugin> getPlugin(String pluginName) {
        return Optional.ofNullable(plugins.get(pluginName));
    }
    
    public List<Plugin> getPluginsByTrigger(String trigger) {
        return new ArrayList<>(pluginsByTrigger.getOrDefault(trigger, new ArrayList<>()));
    }
    
    public List<Plugin> getPluginsByType(String pluginType) {
        return new ArrayList<>(pluginsByType.getOrDefault(pluginType, new ArrayList<>()));
    }
    
    public List<String> getAllPluginNames() {
        return new ArrayList<>(plugins.keySet());
    }
    
    public List<Plugin> getAllPlugins() {
        return new ArrayList<>(plugins.values());
    }
    
    public void clear() {
        plugins.clear();
        pluginsByTrigger.clear();
        pluginsByType.clear();
        log.info("Plugin registry cleared");
    }
    
    public int getPluginCount() {
        return plugins.size();
    }
    
    public interface TriggerBasedPlugin {
        String getTrigger();
    }
}
