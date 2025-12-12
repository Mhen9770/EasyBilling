package com.easybilling.dto;

import com.easybilling.enums.CreditNoteReason;
import com.easybilling.enums.CreditNoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditNoteResponse {
    private String id;
    private String creditNoteNumber;
    private String invoiceId;
    private String customerId;
    private String customerName;
    private CreditNoteStatus status;
    private CreditNoteReason reason;
    private List<CreditNoteItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private Boolean appliedToInvoice;
    private Boolean refundIssued;
    private Boolean storeCredit;
    private BigDecimal refundAmount;
    private BigDecimal storeCreditAmount;
    private String refundReferenceNumber;
    private LocalDateTime refundedAt;
    private String notes;
    private String internalNotes;
    private Boolean requiresApproval;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String approvalNotes;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime issuedAt;
}
