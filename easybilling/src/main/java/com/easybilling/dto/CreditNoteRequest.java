package com.easybilling.dto;

import com.easybilling.enums.CreditNoteReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditNoteRequest {
    private String invoiceId;
    private CreditNoteReason reason;
    private List<CreditNoteItemRequest> items;
    private Boolean appliedToInvoice;
    private Boolean refundIssued;
    private Boolean storeCredit;
    private String notes;
    private String internalNotes;
    private Boolean requiresApproval;
}
