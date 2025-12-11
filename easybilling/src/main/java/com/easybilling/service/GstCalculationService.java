package com.easybilling.service;

import com.easybilling.dto.GstCalculation;
import com.easybilling.entity.GstRate;
import com.easybilling.exception.BusinessException;
import com.easybilling.repository.GstRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Service for GST calculation and validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GstCalculationService {
    
    private final GstRateRepository gstRateRepository;
    
    /**
     * Calculate GST for a given amount and HSN/SAC code.
     */
    @Transactional(readOnly = true)
    public GstCalculation calculateGst(
            String hsnOrSacCode,
            BigDecimal amount,
            String supplierState,
            String customerState) {
        
        log.debug("Calculating GST for code: {}, amount: {}, supplier state: {}, customer state: {}", 
                hsnOrSacCode, amount, supplierState, customerState);
        
        // Find GST rate
        GstRate rate = findGstRate(hsnOrSacCode);
        
        // Determine if inter-state or intra-state
        boolean isInterstate = !supplierState.equalsIgnoreCase(customerState);
        
        return calculateGstWithRate(rate, amount, isInterstate);
    }
    
    /**
     * Calculate GST using tax category.
     */
    @Transactional(readOnly = true)
    public GstCalculation calculateGstByCategory(
            String taxCategory,
            BigDecimal amount,
            boolean isInterstate) {
        
        log.debug("Calculating GST for category: {}, amount: {}, interstate: {}", 
                taxCategory, amount, isInterstate);
        
        GstRate rate = gstRateRepository.findByTaxCategoryAndIsActiveTrue(taxCategory)
                .orElseThrow(() -> new BusinessException("GST_RATE_NOT_FOUND", 
                    "GST rate not found for category: " + taxCategory));
        
        return calculateGstWithRate(rate, amount, isInterstate);
    }
    
    /**
     * Calculate GST with a specific rate.
     */
    private GstCalculation calculateGstWithRate(GstRate rate, BigDecimal amount, boolean isInterstate) {
        GstCalculation calculation = GstCalculation.builder()
                .taxableAmount(amount)
                .isInterstate(isInterstate)
                .cgstRate(rate.getCgstRate())
                .sgstRate(rate.getSgstRate())
                .igstRate(rate.getIgstRate())
                .cessRate(rate.getCessRate())
                .build();
        
        if (isInterstate) {
            // IGST for inter-state
            calculation.setIgst(calculatePercentage(amount, rate.getIgstRate()));
            calculation.setCgst(BigDecimal.ZERO);
            calculation.setSgst(BigDecimal.ZERO);
        } else {
            // CGST + SGST for intra-state
            calculation.setCgst(calculatePercentage(amount, rate.getCgstRate()));
            calculation.setSgst(calculatePercentage(amount, rate.getSgstRate()));
            calculation.setIgst(BigDecimal.ZERO);
        }
        
        calculation.setCess(calculatePercentage(amount, rate.getCessRate()));
        
        BigDecimal totalTax = calculation.getCgst()
                .add(calculation.getSgst())
                .add(calculation.getIgst())
                .add(calculation.getCess());
        calculation.setTotalTax(totalTax);
        
        log.debug("GST calculated - CGST: {}, SGST: {}, IGST: {}, CESS: {}, Total: {}", 
                calculation.getCgst(), calculation.getSgst(), calculation.getIgst(), 
                calculation.getCess(), totalTax);
        
        return calculation;
    }
    
    /**
     * Find GST rate by HSN or SAC code.
     */
    private GstRate findGstRate(String code) {
        // Try HSN first
        return gstRateRepository.findByHsnCodeAndIsActiveTrue(code)
                .or(() -> gstRateRepository.findBySacCodeAndIsActiveTrue(code))
                .orElseThrow(() -> new BusinessException("GST_RATE_NOT_FOUND", 
                    "GST rate not found for code: " + code));
    }
    
    /**
     * Calculate percentage of amount.
     */
    private BigDecimal calculatePercentage(BigDecimal amount, BigDecimal percentage) {
        if (amount == null || percentage == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Validate GSTIN format.
     */
    public boolean validateGstin(String gstin) {
        if (gstin == null || gstin.length() != 15) {
            return false;
        }
        
        // Format: 22AAAAA0000A1Z5
        // First 2: State code (01-37)
        // Next 10: PAN (AAAAA0000A)
        // 13th: Entity number (1-9, A-Z)
        // 14th: Z (default)
        // 15th: Checksum digit
        
        String regex = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$";
        return gstin.matches(regex);
    }
    
    /**
     * Extract state code from GSTIN.
     */
    public String extractStateCodeFromGstin(String gstin) {
        if (gstin == null || gstin.length() < 2) {
            return null;
        }
        return gstin.substring(0, 2);
    }
    
    /**
     * Extract PAN from GSTIN.
     */
    public String extractPanFromGstin(String gstin) {
        if (gstin == null || gstin.length() < 12) {
            return null;
        }
        return gstin.substring(2, 12);
    }
}
