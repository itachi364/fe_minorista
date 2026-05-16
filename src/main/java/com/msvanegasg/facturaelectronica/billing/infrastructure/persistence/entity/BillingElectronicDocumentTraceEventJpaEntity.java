package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentTraceAction;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "billing_electronic_document_trace_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingElectronicDocumentTraceEventJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false, length = 40)
    private ElectronicDocumentStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 40)
    private ElectronicDocumentStatus newStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private ElectronicDocumentTraceAction action;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ProviderSubmissionStatus result;

    @Column
    private String detail;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
