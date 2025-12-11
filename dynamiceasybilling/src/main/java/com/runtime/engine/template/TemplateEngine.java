package com.runtime.engine.template;

import com.runtime.engine.pipeline.RuntimeExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class TemplateEngine {
    
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    
    public String render(TemplateDefinition template, Map<String, Object> data, RuntimeExecutionContext context) {
        if (template == null || template.getTemplateContent() == null) {
            return "";
        }
        
        log.debug("Rendering template: {}", template.getName());
        
        String content = template.getTemplateContent();
        
        content = replaceVariables(content, data);
        content = replaceContextVariables(content, context);
        
        return content;
    }
    
    public String renderFromString(String templateContent, Map<String, Object> data, RuntimeExecutionContext context) {
        if (templateContent == null || templateContent.isEmpty()) {
            return "";
        }
        
        String content = replaceVariables(templateContent, data);
        content = replaceContextVariables(content, context);
        
        return content;
    }
    
    private String replaceVariables(String content, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return content;
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = getNestedValue(data, variableName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    private String replaceContextVariables(String content, RuntimeExecutionContext context) {
        if (context == null) {
            return content;
        }
        
        content = content.replace("${tenantId}", context.getTenantId() != null ? context.getTenantId() : "");
        content = content.replace("${userId}", context.getUserId() != null ? context.getUserId() : "");
        content = content.replace("${sessionId}", context.getSessionId() != null ? context.getSessionId() : "");
        
        if (context.getVariables() != null) {
            for (Map.Entry<String, Object> entry : context.getVariables().entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                content = content.replace(placeholder, value);
            }
        }
        
        return content;
    }
    
    private Object getNestedValue(Map<String, Object> data, String key) {
        if (!key.contains(".")) {
            return data.get(key);
        }
        
        String[] parts = key.split("\\.");
        Object current = data;
        
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }
        
        return current;
    }
}
