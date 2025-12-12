package com.easybilling.enums;

/**
 * Reason for issuing a credit note
 */
public enum CreditNoteReason {
    PRODUCT_RETURN,       // Customer returned product
    DEFECTIVE_PRODUCT,    // Product was defective
    INCORRECT_PRICING,    // Wrong price charged
    DUPLICATE_INVOICE,    // Invoice was duplicate
    SERVICE_NOT_PROVIDED, // Service was not delivered
    CUSTOMER_DISSATISFACTION, // Customer complaint/goodwill
    BILLING_ERROR,        // General billing error
    DAMAGED_GOODS,        // Goods damaged in transit
    ORDER_CANCELLED,      // Order cancelled after invoicing
    OTHER                 // Other reason (specify in notes)
}
