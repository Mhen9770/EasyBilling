package com.easybilling.service;

import com.easybilling.dto.WebhookDTO;
import com.easybilling.entity.Webhook;
import com.easybilling.exception.ResourceNotFoundException;
import com.easybilling.repository.WebhookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing webhooks and triggering external integrations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {
    
    private final WebhookRepository webhookRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    
    @Transactional(readOnly = true)
    public List<WebhookDTO> getAllWebhooks(Integer tenantId) {
        return webhookRepository.findByTenantId(tenantId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<WebhookDTO> getWebhooksByEvent(Integer tenantId, String eventType) {
        return webhookRepository.findByTenantIdAndEventTypeAndIsActiveTrue(tenantId, eventType).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<WebhookDTO> getActiveWebhooks(Integer tenantId) {
        return webhookRepository.findByTenantIdAndIsActiveTrue(tenantId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public WebhookDTO getWebhookById(Long id, Integer tenantId) {
        return webhookRepository.findByIdAndTenantId(id, tenantId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook not found: " + id));
    }
    
    @Transactional
    public WebhookDTO createWebhook(WebhookDTO dto) {
        Webhook webhook = Webhook.builder()
                .tenantId(dto.getTenantId())
                .webhookName(dto.getWebhookName())
                .description(dto.getDescription())
                .eventType(dto.getEventType())
                .targetUrl(dto.getTargetUrl())
                .httpMethod(dto.getHttpMethod() != null ? dto.getHttpMethod() : "POST")
                .headers(dto.getHeaders())
                .payloadTemplate(dto.getPayloadTemplate())
                .secretKey(dto.getSecretKey())
                .retryCount(dto.getRetryCount() != null ? dto.getRetryCount() : 3)
                .retryInterval(dto.getRetryInterval() != null ? dto.getRetryInterval() : 300)
                .timeout(dto.getTimeout() != null ? dto.getTimeout() : 30)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .successCount(0L)
                .failureCount(0L)
                .createdBy(dto.getCreatedBy())
                .build();
        
        webhook = webhookRepository.save(webhook);
        log.info("Created webhook: {} for tenant: {}", webhook.getWebhookName(), webhook.getTenantId());
        return toDTO(webhook);
    }
    
    @Transactional
    public WebhookDTO updateWebhook(Long id, WebhookDTO dto) {
        Webhook webhook = webhookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook not found: " + id));
        
        webhook.setWebhookName(dto.getWebhookName());
        webhook.setDescription(dto.getDescription());
        webhook.setEventType(dto.getEventType());
        webhook.setTargetUrl(dto.getTargetUrl());
        webhook.setHttpMethod(dto.getHttpMethod());
        webhook.setHeaders(dto.getHeaders());
        webhook.setPayloadTemplate(dto.getPayloadTemplate());
        webhook.setSecretKey(dto.getSecretKey());
        webhook.setRetryCount(dto.getRetryCount());
        webhook.setRetryInterval(dto.getRetryInterval());
        webhook.setTimeout(dto.getTimeout());
        webhook.setIsActive(dto.getIsActive());
        
        webhook = webhookRepository.save(webhook);
        log.info("Updated webhook: {}", id);
        return toDTO(webhook);
    }
    
    @Transactional
    public void deleteWebhook(Long id, Integer tenantId) {
        Webhook webhook = webhookRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook not found: " + id));
        
        webhookRepository.delete(webhook);
        log.info("Deleted webhook: {}", id);
    }
    
    @Transactional
    public void toggleWebhookStatus(Long id, Integer tenantId, Boolean isActive) {
        Webhook webhook = webhookRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook not found: " + id));
        
        webhook.setIsActive(isActive);
        webhookRepository.save(webhook);
        log.info("Set webhook {} status to: {}", id, isActive);
    }
    
    /**
     * Trigger webhooks for a specific event
     * @param eventType The event type
     * @param eventData The data to send to the webhook
     * @param tenantId The tenant ID
     */
    @Transactional
    public void triggerWebhooks(String eventType, Map<String, Object> eventData, Integer tenantId) {
        List<Webhook> webhooks = webhookRepository.findByTenantIdAndEventTypeAndIsActiveTrue(tenantId, eventType);
        
        for (Webhook webhook : webhooks) {
            try {
                sendWebhook(webhook, eventData);
                webhook.setLastTriggeredAt(Instant.now());
                webhook.setSuccessCount(webhook.getSuccessCount() + 1);
                webhookRepository.save(webhook);
                log.info("Triggered webhook: {} for event: {}", webhook.getWebhookName(), eventType);
            } catch (Exception e) {
                log.error("Error triggering webhook: {}", webhook.getWebhookName(), e);
                webhook.setFailureCount(webhook.getFailureCount() + 1);
                webhookRepository.save(webhook);
                
                // Retry logic
                retryWebhook(webhook, eventData);
            }
        }
    }
    
    /**
     * Send webhook HTTP request
     */
    private void sendWebhook(Webhook webhook, Map<String, Object> eventData) {
        try {
            // Prepare payload
            String payload = preparePayload(webhook.getPayloadTemplate(), eventData);
            
            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            if (webhook.getHeaders() != null && !webhook.getHeaders().isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, String> customHeaders = objectMapper.readValue(webhook.getHeaders(), Map.class);
                customHeaders.forEach(headers::set);
            }
            
            // Add signature if secret key is provided
            if (webhook.getSecretKey() != null && !webhook.getSecretKey().isEmpty()) {
                String signature = generateSignature(payload, webhook.getSecretKey());
                headers.set("X-Webhook-Signature", signature);
            }
            
            // Create request entity
            HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);
            
            // Send request based on HTTP method
            ResponseEntity<String> response = switch (webhook.getHttpMethod().toUpperCase()) {
                case "POST" -> restTemplate.postForEntity(webhook.getTargetUrl(), requestEntity, String.class);
                case "PUT" -> restTemplate.exchange(webhook.getTargetUrl(), HttpMethod.PUT, requestEntity, String.class);
                case "PATCH" -> restTemplate.exchange(webhook.getTargetUrl(), HttpMethod.PATCH, requestEntity, String.class);
                default -> throw new IllegalArgumentException("Unsupported HTTP method: " + webhook.getHttpMethod());
            };
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Webhook {} completed successfully", webhook.getWebhookName());
            } else {
                throw new RuntimeException("Webhook returned non-2xx status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending webhook: {}", webhook.getWebhookName(), e);
            throw new RuntimeException("Failed to send webhook", e);
        }
    }
    
    /**
     * Prepare webhook payload from template
     */
    private String preparePayload(String template, Map<String, Object> eventData) {
        if (template == null || template.isEmpty()) {
            try {
                return objectMapper.writeValueAsString(eventData);
            } catch (Exception e) {
                log.error("Error serializing event data", e);
                return "{}";
            }
        }
        
        // Simple template replacement - could be enhanced with a proper template engine
        String payload = template;
        for (Map.Entry<String, Object> entry : eventData.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            payload = payload.replace(placeholder, value);
        }
        
        return payload;
    }
    
    /**
     * Generate HMAC signature for webhook security
     */
    private String generateSignature(String payload, String secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Error generating webhook signature", e);
            return "";
        }
    }
    
    /**
     * Retry failed webhook
     * Note: This is a simple retry implementation. For production, consider using
     * Spring Retry or a message queue for better reliability and async handling.
     */
    private void retryWebhook(Webhook webhook, Map<String, Object> eventData) {
        int maxRetries = webhook.getRetryCount();
        int retryInterval = webhook.getRetryInterval();
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Thread.sleep(retryInterval * 1000L);
                sendWebhook(webhook, eventData);
                webhook.setSuccessCount(webhook.getSuccessCount() + 1);
                webhookRepository.save(webhook);
                log.info("Webhook {} succeeded on retry attempt {}", webhook.getWebhookName(), attempt);
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Webhook retry interrupted", e);
                return;
            } catch (Exception e) {
                log.warn("Webhook {} retry attempt {} failed", webhook.getWebhookName(), attempt);
            }
        }
        
        log.error("Webhook {} failed after {} retry attempts", webhook.getWebhookName(), maxRetries);
    }
    
    /**
     * Test webhook connection
     */
    public boolean testWebhook(Long id, Integer tenantId) {
        Webhook webhook = webhookRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Webhook not found: " + id));
        
        try {
            Map<String, Object> testData = Map.of(
                "test", true,
                "webhookId", webhook.getId(),
                "webhookName", webhook.getWebhookName(),
                "timestamp", Instant.now().toString()
            );
            
            sendWebhook(webhook, testData);
            return true;
        } catch (Exception e) {
            log.error("Webhook test failed", e);
            return false;
        }
    }
    
    private WebhookDTO toDTO(Webhook entity) {
        return WebhookDTO.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .webhookName(entity.getWebhookName())
                .description(entity.getDescription())
                .eventType(entity.getEventType())
                .targetUrl(entity.getTargetUrl())
                .httpMethod(entity.getHttpMethod())
                .headers(entity.getHeaders())
                .payloadTemplate(entity.getPayloadTemplate())
                .secretKey(entity.getSecretKey() != null ? "***" : null) // Mask secret key
                .retryCount(entity.getRetryCount())
                .retryInterval(entity.getRetryInterval())
                .timeout(entity.getTimeout())
                .isActive(entity.getIsActive())
                .lastTriggeredAt(entity.getLastTriggeredAt())
                .successCount(entity.getSuccessCount())
                .failureCount(entity.getFailureCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }
}
