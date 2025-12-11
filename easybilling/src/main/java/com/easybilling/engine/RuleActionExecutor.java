package com.easybilling.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Executes rule actions when conditions are met.
 * Supports various action types like applying discounts, sending notifications, etc.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RuleActionExecutor {
    
    /**
     * Execute a list of actions.
     * 
     * @param actions List of actions to execute
     * @param context The rule context
     * @param parameters Additional rule parameters
     * @return Result of action execution
     */
    public RuleActionResult execute(List<Map<String, Object>> actions, 
                                    RuleContext context,
                                    Map<String, Object> parameters) {
        RuleActionResult result = new RuleActionResult();
        
        if (actions == null || actions.isEmpty()) {
            log.debug("No actions to execute");
            return result;
        }
        
        for (Map<String, Object> action : actions) {
            try {
                executeAction(action, context, parameters, result);
            } catch (Exception e) {
                log.error("Error executing action: {}", action.get("type"), e);
                result.addError("Action execution failed: " + e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * Execute a single action.
     */
    @SuppressWarnings("unchecked")
    private void executeAction(Map<String, Object> action, 
                               RuleContext context,
                               Map<String, Object> parameters,
                               RuleActionResult result) {
        String actionType = (String) action.get("type");
        
        log.debug("Executing action: {}", actionType);
        
        switch (actionType.toLowerCase()) {
            case "applydiscount":
            case "apply_discount":
                applyDiscount(action, context, parameters, result);
                break;
                
            case "addmessage":
            case "add_message":
                addMessage(action, result);
                break;
                
            case "setvalue":
            case "set_value":
                setValue(action, context, result);
                break;
                
            case "updatestatus":
            case "update_status":
                updateStatus(action, context, result);
                break;
                
            case "sendnotification":
            case "send_notification":
                sendNotification(action, context, result);
                break;
                
            case "requireapproval":
            case "require_approval":
                requireApproval(action, context, result);
                break;
                
            case "createpurchaseorder":
            case "create_purchase_order":
                createPurchaseOrder(action, context, result);
                break;
                
            default:
                log.warn("Unknown action type: {}", actionType);
                result.addError("Unknown action type: " + actionType);
        }
    }
    
    /**
     * Apply discount action.
     */
    private void applyDiscount(Map<String, Object> action, 
                              RuleContext context,
                              Map<String, Object> parameters,
                              RuleActionResult result) {
        String discountType = (String) action.get("discountType");
        Object value = action.get("value");
        String applyTo = (String) action.getOrDefault("applyTo", "total");
        
        BigDecimal discountValue = toBigDecimal(value);
        BigDecimal amount = toBigDecimal(context.getValue(applyTo));
        
        BigDecimal discountAmount;
        if ("percentage".equalsIgnoreCase(discountType)) {
            discountAmount = amount.multiply(discountValue)
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            discountAmount = discountValue;
        }
        
        // Apply maximum discount limit if specified
        if (parameters != null && parameters.containsKey("maxDiscountAmount")) {
            BigDecimal maxDiscount = toBigDecimal(parameters.get("maxDiscountAmount"));
            if (discountAmount.compareTo(maxDiscount) > 0) {
                discountAmount = maxDiscount;
            }
        }
        
        result.addResult("discountAmount", discountAmount);
        result.addResult("discountType", discountType);
        result.addResult("discountValue", discountValue);
        
        log.info("Applied {} discount of {}: amount={}", discountType, discountValue, discountAmount);
    }
    
    /**
     * Add message action.
     */
    private void addMessage(Map<String, Object> action, RuleActionResult result) {
        String message = (String) action.get("message");
        result.addMessage(message);
        log.debug("Added message: {}", message);
    }
    
    /**
     * Set value action.
     */
    private void setValue(Map<String, Object> action, 
                         RuleContext context,
                         RuleActionResult result) {
        String field = (String) action.get("field");
        Object value = action.get("value");
        
        context.setValue(field, value);
        result.addResult("setValue_" + field, value);
        
        log.debug("Set value: {}={}", field, value);
    }
    
    /**
     * Update status action.
     */
    private void updateStatus(Map<String, Object> action, 
                             RuleContext context,
                             RuleActionResult result) {
        String newStatus = (String) action.get("newStatus");
        
        context.setValue("status", newStatus);
        result.addResult("newStatus", newStatus);
        
        log.info("Updated status to: {}", newStatus);
    }
    
    /**
     * Send notification action.
     * This is a placeholder - actual implementation would integrate with notification service.
     */
    private void sendNotification(Map<String, Object> action, 
                                  RuleContext context,
                                  RuleActionResult result) {
        List<?> recipients = (List<?>) action.get("recipients");
        String template = (String) action.get("template");
        
        result.addResult("notificationSent", true);
        result.addResult("notificationRecipients", recipients);
        result.addResult("notificationTemplate", template);
        result.addMessage("Notification sent to: " + recipients);
        
        log.info("Notification scheduled: template={}, recipients={}", template, recipients);
    }
    
    /**
     * Require approval action.
     */
    private void requireApproval(Map<String, Object> action, 
                                RuleContext context,
                                RuleActionResult result) {
        String approverRole = (String) action.get("approverRole");
        Integer approvalCount = (Integer) action.getOrDefault("approvalCount", 1);
        
        context.setValue("requiresApproval", true);
        context.setValue("approverRole", approverRole);
        context.setValue("approvalCount", approvalCount);
        
        result.addResult("requiresApproval", true);
        result.addResult("approverRole", approverRole);
        result.addMessage("Approval required from: " + approverRole);
        
        log.info("Approval required: role={}, count={}", approverRole, approvalCount);
    }
    
    /**
     * Create purchase order action.
     * This is a placeholder - actual implementation would integrate with inventory service.
     */
    private void createPurchaseOrder(Map<String, Object> action, 
                                    RuleContext context,
                                    RuleActionResult result) {
        Object quantity = action.get("quantity");
        String supplier = (String) action.get("supplier");
        
        result.addResult("purchaseOrderCreated", true);
        result.addResult("purchaseOrderQuantity", quantity);
        result.addResult("purchaseOrderSupplier", supplier);
        result.addMessage("Purchase order created: " + quantity + " units from " + supplier);
        
        log.info("Purchase order scheduled: quantity={}, supplier={}", quantity, supplier);
    }
    
    /**
     * Convert object to BigDecimal.
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        } else {
            return new BigDecimal(value.toString());
        }
    }
}
