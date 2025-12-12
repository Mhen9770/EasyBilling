package com.easybilling.enums;

/**
 * Status options for Quote/Estimate
 */
public enum QuoteStatus {
    DRAFT,          // Being prepared
    SENT,           // Sent to customer
    VIEWED,         // Customer viewed the quote
    ACCEPTED,       // Customer accepted, ready to convert to invoice
    REJECTED,       // Customer rejected
    EXPIRED,        // Validity period expired
    CONVERTED       // Already converted to invoice
}
