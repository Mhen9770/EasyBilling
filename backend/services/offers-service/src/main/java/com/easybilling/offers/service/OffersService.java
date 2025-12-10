package com.easybilling.offers.service;

import com.easybilling.offers.dto.OfferRequest;
import com.easybilling.offers.dto.OfferResponse;
import com.easybilling.offers.entity.Offer;
import com.easybilling.offers.enums.OfferStatus;
import com.easybilling.offers.repository.OfferRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OffersService {
    
    private final OfferRepository offerRepository;
    private final ObjectMapper objectMapper;
    
    public OfferResponse createOffer(OfferRequest request, String tenantId) {
        log.info("Creating offer for tenant: {}", tenantId);
        
        Offer offer = Offer.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .status(OfferStatus.DRAFT)
                .discountValue(request.getDiscountValue())
                .minimumPurchaseAmount(request.getMinimumPurchaseAmount())
                .maximumDiscountAmount(request.getMaximumDiscountAmount())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .usageLimit(request.getUsageLimit())
                .usageCount(0)
                .applicableProducts(toJson(request.getApplicableProducts()))
                .applicableCategories(toJson(request.getApplicableCategories()))
                .stackable(request.getStackable() != null ? request.getStackable() : false)
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .termsAndConditions(request.getTermsAndConditions())
                .build();
        
        Offer saved = offerRepository.save(offer);
        return mapToResponse(saved);
    }
    
    @Transactional(readOnly = true)
    public Page<OfferResponse> getAllOffers(String tenantId, Pageable pageable) {
        return offerRepository.findByTenantId(tenantId, pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public OfferResponse getOfferById(String id, String tenantId) {
        Offer offer = offerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        return mapToResponse(offer);
    }
    
    @Transactional(readOnly = true)
    public List<OfferResponse> getActiveOffers(String tenantId) {
        return offerRepository.findActiveOffers(tenantId, LocalDateTime.now())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    public OfferResponse updateOffer(String id, OfferRequest request, String tenantId) {
        Offer offer = offerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        
        offer.setName(request.getName());
        offer.setDescription(request.getDescription());
        offer.setType(request.getType());
        offer.setDiscountValue(request.getDiscountValue());
        offer.setMinimumPurchaseAmount(request.getMinimumPurchaseAmount());
        offer.setMaximumDiscountAmount(request.getMaximumDiscountAmount());
        offer.setValidFrom(request.getValidFrom());
        offer.setValidTo(request.getValidTo());
        offer.setUsageLimit(request.getUsageLimit());
        offer.setApplicableProducts(toJson(request.getApplicableProducts()));
        offer.setApplicableCategories(toJson(request.getApplicableCategories()));
        if (request.getStackable() != null) offer.setStackable(request.getStackable());
        if (request.getPriority() != null) offer.setPriority(request.getPriority());
        offer.setTermsAndConditions(request.getTermsAndConditions());
        
        Offer updated = offerRepository.save(offer);
        return mapToResponse(updated);
    }
    
    public OfferResponse activateOffer(String id, String tenantId) {
        Offer offer = offerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        offer.setStatus(OfferStatus.ACTIVE);
        Offer updated = offerRepository.save(offer);
        return mapToResponse(updated);
    }
    
    public OfferResponse pauseOffer(String id, String tenantId) {
        Offer offer = offerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        offer.setStatus(OfferStatus.PAUSED);
        Offer updated = offerRepository.save(offer);
        return mapToResponse(updated);
    }
    
    public void deleteOffer(String id, String tenantId) {
        Offer offer = offerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        offerRepository.delete(offer);
    }
    
    private OfferResponse mapToResponse(Offer offer) {
        OfferResponse response = new OfferResponse();
        response.setId(offer.getId());
        response.setName(offer.getName());
        response.setDescription(offer.getDescription());
        response.setType(offer.getType());
        response.setStatus(offer.getStatus());
        response.setDiscountValue(offer.getDiscountValue());
        response.setMinimumPurchaseAmount(offer.getMinimumPurchaseAmount());
        response.setMaximumDiscountAmount(offer.getMaximumDiscountAmount());
        response.setValidFrom(offer.getValidFrom());
        response.setValidTo(offer.getValidTo());
        response.setUsageLimit(offer.getUsageLimit());
        response.setUsageCount(offer.getUsageCount());
        response.setApplicableProducts(offer.getApplicableProducts());
        response.setApplicableCategories(offer.getApplicableCategories());
        response.setStackable(offer.getStackable());
        response.setPriority(offer.getPriority());
        response.setTermsAndConditions(offer.getTermsAndConditions());
        response.setCreatedAt(offer.getCreatedAt());
        response.setUpdatedAt(offer.getUpdatedAt());
        return response;
    }
    
    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error converting to JSON", e);
            return null;
        }
    }
}
