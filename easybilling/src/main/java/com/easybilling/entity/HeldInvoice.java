package com.easybilling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "held_invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class HeldInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String storeId;

    @Column(nullable = false)
    private String counterId;

    @Column(nullable = false)
    private String holdReference;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String invoiceData;

    @Column(nullable = false)
    private String heldBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime heldAt;

    private String notes;
}
