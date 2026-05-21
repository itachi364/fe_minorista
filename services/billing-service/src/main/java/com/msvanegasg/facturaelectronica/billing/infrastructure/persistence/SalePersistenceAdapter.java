package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.billing.application.port.out.SaleRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleLine;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.ElectronicDocumentJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.SaleJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.SaleLineJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.SaleJpaRepository;

@Component
public class SalePersistenceAdapter implements SaleRepositoryPort {

    private final SaleJpaRepository repository;

    public SalePersistenceAdapter(SaleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Sale> findByCompanyIdAndId(UUID companyId, UUID id) {
        return repository.findByCompanyIdAndId(companyId, id).map(SalePersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Sale> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey) {
        return repository.findByCompanyIdAndIdempotencyKey(companyId, idempotencyKey)
                .map(SalePersistenceAdapter::toDomain);
    }

    @Override
    public Sale save(Sale sale) {
        return toDomain(repository.save(toEntity(sale)));
    }

    private static Sale toDomain(SaleJpaEntity entity) {
        return new Sale(entity.getId(), entity.getCompanyId(), entity.getCustomerId(), entity.getPaymentMethodId(),
                entity.getSaleChannel(), entity.getStatus(), entity.getSubtotal(), entity.getDiscountTotal(),
                entity.getTaxTotal(), entity.getTotal(), entity.getIdempotencyKey(), entity.getCreatedBy(),
                entity.getCreatedAt(), entity.getConfirmedAt(),
                entity.getLines().stream().map(SalePersistenceAdapter::toLineDomain).toList(),
                entity.getElectronicDocument() == null ? null : toDocumentDomain(entity.getId(), entity.getElectronicDocument()));
    }

    private static SaleLine toLineDomain(SaleLineJpaEntity entity) {
        return new SaleLine(entity.getId(), entity.getProductId(), entity.getProductSku(), entity.getProductName(),
                entity.getItemType(), entity.isStockTracked(), entity.getQuantity(), entity.getUnitPrice(),
                entity.getDiscountAmount(), entity.getTaxCode(), entity.getTaxRate(), entity.getSubtotal(),
                entity.getTaxAmount(), entity.getTotal());
    }

    private static ElectronicDocument toDocumentDomain(UUID saleId, ElectronicDocumentJpaEntity entity) {
        return new ElectronicDocument(entity.getId(), entity.getCompanyId(), saleId, entity.getDocumentType(),
                entity.getStatus(), entity.getProviderStatus(), entity.getPrefix(), entity.getDocumentNumber(),
                entity.getCufeCude(), entity.getQrContent(), entity.getSubtotal(), entity.getTaxTotal(),
                entity.getTotal(), entity.getProviderTrackingId(), entity.getProviderErrorCode(),
                entity.getProviderErrorMessage(), entity.getIdempotencyKey(), entity.getIssuedAt(),
                entity.getInventoryAppliedAt(), entity.getAccountingAppliedAt());
    }

    private static SaleJpaEntity toEntity(Sale sale) {
        SaleJpaEntity entity = new SaleJpaEntity();
        entity.setId(sale.id());
        entity.setCompanyId(sale.companyId());
        entity.setCustomerId(sale.customerId());
        entity.setPaymentMethodId(sale.paymentMethodId());
        entity.setSaleChannel(sale.saleChannel());
        entity.setStatus(sale.status());
        entity.setSubtotal(sale.subtotal());
        entity.setDiscountTotal(sale.discountTotal());
        entity.setTaxTotal(sale.taxTotal());
        entity.setTotal(sale.total());
        entity.setIdempotencyKey(sale.idempotencyKey());
        entity.setCreatedBy(sale.createdBy());
        entity.setCreatedAt(sale.createdAt());
        entity.setConfirmedAt(sale.confirmedAt());
        entity.replaceLines(sale.lines().stream().map(SalePersistenceAdapter::toLineEntity).toList());
        if (sale.electronicDocument() != null) {
            entity.setElectronicDocument(toDocumentEntity(sale.electronicDocument()));
        }
        return entity;
    }

    private static SaleLineJpaEntity toLineEntity(SaleLine line) {
        SaleLineJpaEntity entity = new SaleLineJpaEntity();
        entity.setId(line.id());
        entity.setProductId(line.productId());
        entity.setProductSku(line.productSku());
        entity.setProductName(line.productName());
        entity.setItemType(line.itemType());
        entity.setStockTracked(line.stockTracked());
        entity.setQuantity(line.quantity());
        entity.setUnitPrice(line.unitPrice());
        entity.setDiscountAmount(line.discountAmount());
        entity.setTaxCode(line.taxCode());
        entity.setTaxRate(line.taxRate());
        entity.setSubtotal(line.subtotal());
        entity.setTaxAmount(line.taxAmount());
        entity.setTotal(line.total());
        return entity;
    }

    private static ElectronicDocumentJpaEntity toDocumentEntity(ElectronicDocument document) {
        ElectronicDocumentJpaEntity entity = new ElectronicDocumentJpaEntity();
        entity.setId(document.id());
        entity.setCompanyId(document.companyId());
        entity.setDocumentType(document.documentType());
        entity.setStatus(document.status());
        entity.setProviderStatus(document.providerStatus());
        entity.setPrefix(document.prefix());
        entity.setDocumentNumber(document.documentNumber());
        entity.setCufeCude(document.cufeCude());
        entity.setQrContent(document.qrContent());
        entity.setSubtotal(document.subtotal());
        entity.setTaxTotal(document.taxTotal());
        entity.setTotal(document.total());
        entity.setProviderTrackingId(document.providerTrackingId());
        entity.setProviderErrorCode(document.providerErrorCode());
        entity.setProviderErrorMessage(document.providerErrorMessage());
        entity.setIdempotencyKey(document.idempotencyKey());
        entity.setIssuedAt(document.issuedAt());
        entity.setInventoryAppliedAt(document.inventoryAppliedAt());
        entity.setAccountingAppliedAt(document.accountingAppliedAt());
        return entity;
    }
}
