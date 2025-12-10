package com.easybilling.billing.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for communicating with Inventory Service
 * Handles stock validation, reservation, and updates
 */
@Component
@Slf4j
public class InventoryClient {

    private final RestTemplate restTemplate;
    private final String inventoryServiceUrl;

    public InventoryClient(RestTemplate restTemplate,
                          @Value("${inventory.service.url:http://localhost:8084}") String inventoryServiceUrl) {
        this.restTemplate = restTemplate;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }

    /**
     * Check if product has sufficient stock
     */
    public boolean checkStockAvailability(String productId, String locationId, BigDecimal quantity, String tenantId) {
        try {
            String url = inventoryServiceUrl + "/inventory-service/api/v1/stock/product/" + productId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Tenant-Id", tenantId);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<StockResponse> response = restTemplate.exchange(
                    url, 
                    HttpMethod.GET, 
                    entity, 
                    StockResponse.class
            );
            
            if (response.getBody() != null && response.getBody().getData() != null) {
                StockData stock = response.getBody().getData();
                BigDecimal available = stock.getTotalQuantity().subtract(stock.getReservedQuantity());
                return available.compareTo(quantity) >= 0;
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to check stock availability for product: {}", productId, e);
            // In case of service unavailability, allow the transaction to proceed
            // This prevents blocking sales due to temporary service issues
            return true;
        }
    }

    /**
     * Deduct stock when invoice is completed
     */
    public void deductStock(String productId, String locationId, BigDecimal quantity, String referenceId, String performedBy, String tenantId) {
        try {
            String url = inventoryServiceUrl + "/inventory-service/api/v1/stock/movement";
            
            Map<String, Object> request = new HashMap<>();
            request.put("productId", productId);
            request.put("movementType", "OUT");
            request.put("quantity", quantity);
            request.put("locationId", locationId);
            request.put("referenceId", referenceId);
            request.put("referenceType", "SALE");
            request.put("performedBy", performedBy);
            request.put("notes", "Sale - Invoice: " + referenceId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Tenant-Id", tenantId);
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info("Deducted stock for product: {}, quantity: {}", productId, quantity);
        } catch (Exception e) {
            log.error("Failed to deduct stock for product: {}", productId, e);
            // Don't throw exception - the sale should complete even if stock update fails
            // This can be reconciled later through reports
        }
    }

    /**
     * Reverse stock deduction (for returns/cancellations)
     */
    public void reverseStockDeduction(String productId, String locationId, BigDecimal quantity, String referenceId, String performedBy, String tenantId) {
        try {
            String url = inventoryServiceUrl + "/inventory-service/api/v1/stock/movement";
            
            Map<String, Object> request = new HashMap<>();
            request.put("productId", productId);
            request.put("movementType", "IN");
            request.put("quantity", quantity);
            request.put("locationId", locationId);
            request.put("referenceId", referenceId);
            request.put("referenceType", "RETURN");
            request.put("performedBy", performedBy);
            request.put("notes", "Return - Invoice: " + referenceId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Tenant-Id", tenantId);
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            log.info("Reversed stock deduction for product: {}, quantity: {}", productId, quantity);
        } catch (Exception e) {
            log.error("Failed to reverse stock deduction for product: {}", productId, e);
        }
    }

    // DTOs for stock responses
    private static class StockResponse {
        private StockData data;
        
        public StockData getData() {
            return data;
        }
        
        public void setData(StockData data) {
            this.data = data;
        }
    }
    
    private static class StockData {
        private BigDecimal totalQuantity;
        private BigDecimal reservedQuantity;
        
        public BigDecimal getTotalQuantity() {
            return totalQuantity;
        }
        
        public void setTotalQuantity(BigDecimal totalQuantity) {
            this.totalQuantity = totalQuantity;
        }
        
        public BigDecimal getReservedQuantity() {
            return reservedQuantity;
        }
        
        public void setReservedQuantity(BigDecimal reservedQuantity) {
            this.reservedQuantity = reservedQuantity;
        }
    }
}
