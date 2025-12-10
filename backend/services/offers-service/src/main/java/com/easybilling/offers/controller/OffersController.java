package com.easybilling.offers.controller;

import com.easybilling.offers.dto.OfferRequest;
import com.easybilling.offers.dto.OfferResponse;
import com.easybilling.offers.service.OffersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/offers")
@RequiredArgsConstructor
@Slf4j
public class OffersController {
    
    private final OffersService offersService;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOffer(
            @Valid @RequestBody OfferRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        log.info("Creating offer for tenant: {}", tenantId);
        OfferResponse response = offersService.createOffer(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createSuccessResponse(response));
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOffers(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching offers for tenant: {}", tenantId);
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OfferResponse> offers = offersService.getAllOffers(tenantId, pageRequest);
        
        return ResponseEntity.ok(createPageResponse(offers));
    }
    
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveOffers(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        log.info("Fetching active offers for tenant: {}", tenantId);
        List<OfferResponse> offers = offersService.getActiveOffers(tenantId);
        return ResponseEntity.ok(createSuccessResponse(offers));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOfferById(
            @PathVariable String id,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        log.info("Fetching offer {} for tenant: {}", id, tenantId);
        OfferResponse response = offersService.getOfferById(id, tenantId);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateOffer(
            @PathVariable String id,
            @Valid @RequestBody OfferRequest request,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        log.info("Updating offer {} for tenant: {}", id, tenantId);
        OfferResponse response = offersService.updateOffer(id, request, tenantId);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PostMapping("/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateOffer(
            @PathVariable String id,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        log.info("Activating offer {} for tenant: {}", id, tenantId);
        OfferResponse response = offersService.activateOffer(id, tenantId);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @PostMapping("/{id}/pause")
    public ResponseEntity<Map<String, Object>> pauseOffer(
            @PathVariable String id,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        log.info("Pausing offer {} for tenant: {}", id, tenantId);
        OfferResponse response = offersService.pauseOffer(id, tenantId);
        return ResponseEntity.ok(createSuccessResponse(response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteOffer(
            @PathVariable String id,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        log.info("Deleting offer {} for tenant: {}", id, tenantId);
        offersService.deleteOffer(id, tenantId);
        return ResponseEntity.ok(createSuccessResponse("Offer deleted successfully"));
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
