package com.easybilling.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for generating Indian financial year based invoice numbers.
 * Format: INV/2024-25/0001
 */
@Slf4j
@Service
public class InvoiceNumberService {
    
    // In-memory counter per tenant and financial year
    // In production, this should be stored in database or Redis
    private final ConcurrentHashMap<String, AtomicInteger> sequenceCounters = new ConcurrentHashMap<>();
    
    /**
     * Generate invoice number in Indian financial year format.
     * Format: INV/YYYY-YY/NNNN
     * Example: INV/2024-25/0001
     */
    public String generateInvoiceNumber(String tenantId, String prefix) {
        String financialYear = getCurrentFinancialYear();
        String key = tenantId + "_" + financialYear;
        
        AtomicInteger counter = sequenceCounters.computeIfAbsent(key, k -> new AtomicInteger(0));
        int sequence = counter.incrementAndGet();
        
        String invoicePrefix = (prefix != null && !prefix.isEmpty()) ? prefix : "INV";
        String invoiceNumber = String.format("%s/%s/%04d", invoicePrefix, financialYear, sequence);
        
        log.debug("Generated invoice number: {} for tenant: {}", invoiceNumber, tenantId);
        return invoiceNumber;
    }
    
    /**
     * Generate invoice number with default prefix.
     */
    public String generateInvoiceNumber(String tenantId) {
        return generateInvoiceNumber(tenantId, "INV");
    }
    
    /**
     * Get current Indian financial year.
     * Financial year in India: April 1 to March 31
     * Example: April 2024 to March 2025 = 2024-25
     */
    public String getCurrentFinancialYear() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        Month month = now.getMonth();
        
        if (month.getValue() >= Month.APRIL.getValue()) {
            // April onwards - current year to next year
            return String.format("%d-%02d", year, (year + 1) % 100);
        } else {
            // January to March - previous year to current year
            return String.format("%d-%02d", year - 1, year % 100);
        }
    }
    
    /**
     * Get financial year for a specific date.
     */
    public String getFinancialYear(LocalDate date) {
        int year = date.getYear();
        Month month = date.getMonth();
        
        if (month.getValue() >= Month.APRIL.getValue()) {
            return String.format("%d-%02d", year, (year + 1) % 100);
        } else {
            return String.format("%d-%02d", year - 1, year % 100);
        }
    }
    
    /**
     * Parse invoice number to extract components.
     */
    public InvoiceNumberComponents parseInvoiceNumber(String invoiceNumber) {
        if (invoiceNumber == null || !invoiceNumber.contains("/")) {
            return null;
        }
        
        String[] parts = invoiceNumber.split("/");
        if (parts.length != 3) {
            return null;
        }
        
        return new InvoiceNumberComponents(parts[0], parts[1], Integer.parseInt(parts[2]));
    }
    
    /**
     * Reset sequence counter (use with caution, typically for new financial year).
     */
    public void resetSequence(String tenantId, String financialYear) {
        String key = tenantId + "_" + financialYear;
        sequenceCounters.put(key, new AtomicInteger(0));
        log.info("Reset invoice sequence for tenant: {} and FY: {}", tenantId, financialYear);
    }
    
    /**
     * Get current sequence number (for display/debugging).
     */
    public int getCurrentSequence(String tenantId) {
        String financialYear = getCurrentFinancialYear();
        String key = tenantId + "_" + financialYear;
        AtomicInteger counter = sequenceCounters.get(key);
        return counter != null ? counter.get() : 0;
    }
    
    /**
     * Components of an invoice number.
     */
    public record InvoiceNumberComponents(String prefix, String financialYear, int sequence) {
        @Override
        public String toString() {
            return String.format("%s/%s/%04d", prefix, financialYear, sequence);
        }
    }
}
