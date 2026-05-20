package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "billing_fiscal_audit_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingFiscalAuditEventJpaEntity {

    @Id
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    @Column(name = "resource_type", nullable = false, length = 80)
    private String resourceType;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(nullable = false, length = 80)
    private String result;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column
    private String detail;
}
