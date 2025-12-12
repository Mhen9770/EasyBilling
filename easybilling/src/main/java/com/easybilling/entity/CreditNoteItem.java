package com.easybilling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Line item for Credit Note entity
 */
@Entity
@Table(name = "credit_note_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditNoteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_note_id", nullable = false)
    private CreditNote creditNote;

    private String invoiceItemId; // Reference to original invoice item

    private String productId;

    @Column(nullable = false)
    private String productName;

    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    // Whether to restock this item
    @Builder.Default
    private Boolean restockItem = true;
}
