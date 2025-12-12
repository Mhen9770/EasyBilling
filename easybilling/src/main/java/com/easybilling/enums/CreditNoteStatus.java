package com.easybilling.enums;

/**
 * Status options for Credit Note
 */
public enum CreditNoteStatus {
    DRAFT,           // Being prepared
    PENDING_APPROVAL, // Waiting for approval
    APPROVED,        // Approved and ready to issue
    ISSUED,          // Issued to customer
    APPLIED,         // Applied to invoice/refunded
    CANCELLED        // Cancelled before issue
}
