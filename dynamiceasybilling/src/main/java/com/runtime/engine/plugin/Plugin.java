package com.runtime.engine.plugin;

import com.runtime.engine.pipeline.RuntimeExecutionContext;

import java.util.Map;

public interface Plugin {
    
    String getName();
    
    String getPluginType();
    
    void execute(Map<String, Object> data, RuntimeExecutionContext context);
    
    void initialize(Map<String, Object> config);
    
    void destroy();
    
    boolean isEnabled();
}
