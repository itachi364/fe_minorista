package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.PaymentMethodCode;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleStatus;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.SaleJpaEntity;

public interface SaleJpaRepositoryCustom {

    List<SaleJpaEntity> findElectronicDocumentsDynamic(UUID companyId, ElectronicDocumentType documentType,
            ElectronicDocumentStatus status, UUID customerId, Instant from, Instant to, String prefix, Long number,
            String cufeCude);

    List<SaleJpaEntity> findSalesDynamic(UUID companyId, SaleStatus status, UUID sellerId, UUID customerId,
            UUID productId, PaymentMethodCode paymentMethodCode, ElectronicDocumentStatus documentStatus,
            Instant from, Instant to);
}
