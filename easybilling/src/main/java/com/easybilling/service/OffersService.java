package com.easybilling.service;

import com.easybilling.dto.OfferRequest;
import com.easybilling.dto.OfferResponse;
import com.easybilling.entity.Offer;
import com.easybilling.enums.OfferStatus;
import com.easybilling.repository.OfferRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    
    // Business Logic Methods
    
    /**
     * Calculate discount amount for a given purchase
     */
    public BigDecimal calculateDiscount(String offerId, String tenantId, BigDecimal purchaseAmount,
                                       List<String> productIds, List<String> categoryIds) {
        Offer offer = offerRepository.findByIdAndTenantId(offerId, tenantId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        
        // Validate offer is active and within validity period
        if (!isOfferValid(offer)) {
            throw new RuntimeException("Offer is not valid");
        }
        
        // Check minimum purchase amount
        if (offer.getMinimumPurchaseAmount() != null &&
            purchaseAmount.compareTo(offer.getMinimumPurchaseAmount()) < 0) {
            log.info("Purchase amount {} is below minimum {}", purchaseAmount, offer.getMinimumPurchaseAmount());
            return BigDecimal.ZERO;
        }
        
        // Check if offer is applicable to products/categories
        if (!isOfferApplicable(offer, productIds, categoryIds)) {
            log.info("Offer not applicable to provided products/categories");
            return BigDecimal.ZERO;
        }
        
        // Calculate discount based on offer type
        BigDecimal discount = switch (offer.getType()) {
            case PERCENTAGE_DISCOUNT -> calculatePercentageDiscount(purchaseAmount, offer.getDiscountValue());
            case FIXED_AMOUNT_DISCOUNT -> offer.getDiscountValue();
            case MINIMUM_PURCHASE -> calculateMinimumPurchaseDiscount(purchaseAmount, offer);
            default -> BigDecimal.ZERO;
        };
        
        // Apply maximum discount cap if set
        if (offer.getMaximumDiscountAmount() != null &&
            discount.compareTo(offer.getMaximumDiscountAmount()) > 0) {
            discount = offer.getMaximumDiscountAmount();
        }
        
        log.info("Calculated discount {} for offer {} on purchase amount {}", discount, offerId, purchaseAmount);
        return discount;
    }
    
    /**
     * Apply offer to a purchase and increment usage count
     */
    public BigDecimal applyOffer(String offerId, String tenantId, BigDecimal purchaseAmount,
                                  List<String> productIds, List<String> categoryIds) {
        Offer offer = offerRepository.findByIdAndTenantId(offerId, tenantId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));
        
        // Check usage limit
        if (offer.getUsageLimit() != null && offer.getUsageCount() >= offer.getUsageLimit()) {
            throw new RuntimeException("Offer usage limit reached");
        }
        
        BigDecimal discount = calculateDiscount(offerId, tenantId, purchaseAmount, productIds, categoryIds);
        
        // Increment usage count
        offer.setUsageCount(offer.getUsageCount() + 1);
        offerRepository.save(offer);
        
        log.info("Applied offer {}. New usage count: {}/{}", offerId, offer.getUsageCount(), offer.getUsageLimit());
        
        return discount;
    }
    
    /**
     * Get applicable offers for a purchase
     */
    @Transactional(readOnly = true)
    public List<OfferResponse> getApplicableOffers(String tenantId, BigDecimal purchaseAmount,
                                                    List<String> productIds, List<String> categoryIds) {
        List<Offer> activeOffers = offerRepository.findActiveOffers(tenantId, LocalDateTime.now());
        
        return activeOffers.stream()
                .filter(offer -> isOfferValid(offer))
                .filter(offer -> isOfferApplicable(offer, productIds, categoryIds))
                .filter(offer -> offer.getMinimumPurchaseAmount() == null ||
                               purchaseAmount.compareTo(offer.getMinimumPurchaseAmount()) >= 0)
                .filter(offer -> offer.getUsageLimit() == null ||
                               offer.getUsageCount() < offer.getUsageLimit())
                .sorted((o1, o2) -> o2.getPriority().compareTo(o1.getPriority()))
                .map(this::mapToResponse)
                .toList();
    }
    
    /**
     * Calculate best offer combination (stacking logic)
     */
    public List<OfferResponse> calculateBestOfferCombination(String tenantId, BigDecimal purchaseAmount,
                                                             List<String> productIds, List<String> categoryIds) {
        List<Offer> activeOffers = offerRepository.findActiveOffers(tenantId, LocalDateTime.now());
        
        // Filter applicable offers
        List<Offer> applicableOffers = activeOffers.stream()
                .filter(offer -> isOfferValid(offer))
                .filter(offer -> isOfferApplicable(offer, productIds, categoryIds))
                .sorted((o1, o2) -> o2.getPriority().compareTo(o1.getPriority()))
                .toList();
        
        // Separate stackable and non-stackable
        List<Offer> stackable = applicableOffers.stream()
                .filter(Offer::getStackable)
                .toList();
        
        List<Offer> nonStackable = applicableOffers.stream()
                .filter(o -> !o.getStackable())
                .toList();
        
        // If there are non-stackable offers, pick the best one
        if (!nonStackable.isEmpty()) {
            Offer bestNonStackable = nonStackable.stream()
                    .max((o1, o2) -> calculateDiscount(o1.getId(), tenantId, purchaseAmount, productIds, categoryIds)
                            .compareTo(calculateDiscount(o2.getId(), tenantId, purchaseAmount, productIds, categoryIds)))
                    .orElse(null);
            
            if (bestNonStackable != null) {
                return List.of(mapToResponse(bestNonStackable));
            }
        }
        
        // Return all stackable offers
        return stackable.stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    // Private helper methods
    
    private boolean isOfferValid(Offer offer) {
        if (offer.getStatus() != OfferStatus.ACTIVE) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(offer.getValidFrom()) && !now.isAfter(offer.getValidTo());
    }
    
    private boolean isOfferApplicable(Offer offer, List<String> productIds, List<String> categoryIds) {
        // If no specific products/categories specified, offer applies to all
        if ((offer.getApplicableProducts() == null || offer.getApplicableProducts().isEmpty()) &&
            (offer.getApplicableCategories() == null || offer.getApplicableCategories().isEmpty())) {
            return true;
        }
        
        // Check if any product matches
        if (offer.getApplicableProducts() != null && productIds != null) {
            try {
                List<String> applicableProducts = objectMapper.readValue(
                        offer.getApplicableProducts(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                if (productIds.stream().anyMatch(applicableProducts::contains)) {
                    return true;
                }
            } catch (JsonProcessingException e) {
                log.error("Error parsing applicable products", e);
            }
        }
        
        // Check if any category matches
        if (offer.getApplicableCategories() != null && categoryIds != null) {
            try {
                List<String> applicableCategories = objectMapper.readValue(
                        offer.getApplicableCategories(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                if (categoryIds.stream().anyMatch(applicableCategories::contains)) {
                    return true;
                }
            } catch (JsonProcessingException e) {
                log.error("Error parsing applicable categories", e);
            }
        }
        
        return false;
    }
    
    private BigDecimal calculatePercentageDiscount(BigDecimal amount, BigDecimal percentage) {
        return amount.multiply(percentage).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    private BigDecimal calculateMinimumPurchaseDiscount(BigDecimal amount, Offer offer) {
        if (offer.getMinimumPurchaseAmount() != null &&
            amount.compareTo(offer.getMinimumPurchaseAmount()) >= 0) {
            return offer.getDiscountValue();
        }
        return BigDecimal.ZERO;
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
