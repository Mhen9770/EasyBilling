package com.easybilling.controller;

import com.easybilling.dto.OfferRequest;
import com.easybilling.dto.OfferResponse;
import com.easybilling.service.OffersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/offers")
@RequiredArgsConstructor
@Slf4j
public class OffersController extends BaseController {
    
    private final OffersService offersService;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOffer(@Valid @RequestBody OfferRequest request) {
        Integer tenantId = getCurrentTenantId();
        log.info("Creating offer for tenant: {}", tenantId);
        OfferResponse response = offersService.createOffer(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createSuccessResponse(response));
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOffers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Integer tenantId = getCurrentTenantId();
        log.info("Fetching offers for tenant: {}", tenantId);
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OfferResponse> offers = offersService.getAllOffers(tenantId, pageRequest);
        
        return ResponseEntity.ok(createPageResponse(offers));
    }
    
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveOffers() {
        Integer tenantId = getCurrentTenantId();
        log.info("Fetching active offers for tenant: {}", tenantId);
        List<OfferResponse> offers = offersService.getActiveOffers(tenantId);
        return ResponseEntity.ok(createSuccessResponse(offers));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOfferById(@PathVariable String id) {
        Integer tenantId = getCurrentTenantId();
        log.info("Fetching offer {} for tenant: {}", id, tenantId);
        OfferResponse response = offersService.getOfferById(id, tenantId);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateOffer(
            @PathVariable String id,
            @Valid @RequestBody OfferRequest request) {
        Integer tenantId = getCurrentTenantId();
        log.info("Updating offer {} for tenant: {}", id, tenantId);
        OfferResponse response = offersService.updateOffer(id, request, tenantId);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PostMapping("/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateOffer(@PathVariable String id) {
        Integer tenantId = getCurrentTenantId();
        log.info("Activating offer {} for tenant: {}", id, tenantId);
        OfferResponse response = offersService.activateOffer(id, tenantId);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PostMapping("/{id}/pause")
    public ResponseEntity<Map<String, Object>> pauseOffer(@PathVariable String id) {
        Integer tenantId = getCurrentTenantId();
        log.info("Pausing offer {} for tenant: {}", id, tenantId);
        OfferResponse response = offersService.pauseOffer(id, tenantId);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteOffer(@PathVariable String id) {
        Integer tenantId = getCurrentTenantId();
        log.info("Deleting offer {} for tenant: {}", id, tenantId);
        offersService.deleteOffer(id, tenantId);
        return ResponseEntity.ok(createSuccessResponse("Offer deleted successfully"));
    }
    
    @PostMapping("/{id}/calculate-discount")
    public ResponseEntity<Map<String, Object>> calculateDiscount(
            @PathVariable String id,
            @RequestParam BigDecimal purchaseAmount,
            @RequestParam(required = false) List<String> productIds,
            @RequestParam(required = false) List<String> categoryIds) {
        Integer tenantId = getCurrentTenantId();
        log.info("Calculating discount for offer {} with purchase amount {}", id, purchaseAmount);
        
        BigDecimal discount = offersService.calculateDiscount(id, tenantId, purchaseAmount, productIds, categoryIds);
        
        Map<String, Object> data = new HashMap<>();
        data.put("offerId", id);
        data.put("purchaseAmount", purchaseAmount);
        data.put("discountAmount", discount);
        data.put("finalAmount", purchaseAmount.subtract(discount));
        
        return ResponseEntity.ok(createSuccessResponse(data));
    }
    
    @PostMapping("/{id}/apply")
    public ResponseEntity<Map<String, Object>> applyOffer(
            @PathVariable String id,
            @RequestParam BigDecimal purchaseAmount,
            @RequestParam(required = false) List<String> productIds,
            @RequestParam(required = false) List<String> categoryIds) {
        Integer tenantId = getCurrentTenantId();
        log.info("Applying offer {} to purchase of {}", id, purchaseAmount);
        
        BigDecimal discount = offersService.applyOffer(id, tenantId, purchaseAmount, productIds, categoryIds);
        
        Map<String, Object> data = new HashMap<>();
        data.put("offerId", id);
        data.put("purchaseAmount", purchaseAmount);
        data.put("discountAmount", discount);
        data.put("finalAmount", purchaseAmount.subtract(discount));
        data.put("applied", true);
        
        return ResponseEntity.ok(createSuccessResponse(data));
    }
    
    @GetMapping("/applicable")
    public ResponseEntity<Map<String, Object>> getApplicableOffers(
            @RequestParam BigDecimal purchaseAmount,
            @RequestParam(required = false) List<String> productIds,
            @RequestParam(required = false) List<String> categoryIds) {
        Integer tenantId = getCurrentTenantId();
        log.info("Getting applicable offers for tenant: {}, amount: {}", tenantId, purchaseAmount);
        
        List<OfferResponse> offers = offersService.getApplicableOffers(tenantId, purchaseAmount, productIds, categoryIds);
        return ResponseEntity.ok(createSuccessResponse(offers));
    }
    
    @GetMapping("/best-combination")
    public ResponseEntity<Map<String, Object>> getBestOfferCombination(
            @RequestParam BigDecimal purchaseAmount,
            @RequestParam(required = false) List<String> productIds,
            @RequestParam(required = false) List<String> categoryIds) {
        Integer tenantId = getCurrentTenantId();
        log.info("Calculating best offer combination for tenant: {}, amount: {}", tenantId, purchaseAmount);
        
        List<OfferResponse> offers = offersService.calculateBestOfferCombination(tenantId, purchaseAmount, productIds, categoryIds);
        return ResponseEntity.ok(createSuccessResponse(offers));
    }
    
    private Map<String, Object> createSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    private Map<String, Object> createPageResponse(Page<?> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        Map<String, Object> pageData = new HashMap<>();
        pageData.put("content", page.getContent());
        pageData.put("page", page.getNumber());
        pageData.put("size", page.getSize());
        pageData.put("totalElements", page.getTotalElements());
        pageData.put("totalPages", page.getTotalPages());
        pageData.put("first", page.isFirst());
        pageData.put("last", page.isLast());
        pageData.put("empty", page.isEmpty());
        
        response.put("data", pageData);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
