package com.easybilling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for GST calculation result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GstCalculation {
    
    private BigDecimal taxableAmount;
    
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal cess;
    
    private BigDecimal totalTax;
    
    private BigDecimal cgstRate;
    private BigDecimal sgstRate;
    private BigDecimal igstRate;
    private BigDecimal cessRate;
    
    private boolean isInterstate;
    
    /**
     * Get total amount including tax.
     */
    public BigDecimal getTotalAmountWithTax() {
        return taxableAmount.add(totalTax);
    }
}
