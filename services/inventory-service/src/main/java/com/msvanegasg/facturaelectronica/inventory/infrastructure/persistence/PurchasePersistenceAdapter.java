package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseQuery;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.PurchaseRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseLine;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.PurchaseJpaEntity;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.PurchaseLineJpaEntity;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository.PurchaseJpaRepository;

@Component
public class PurchasePersistenceAdapter implements PurchaseRepositoryPort {

    private final PurchaseJpaRepository repository;

    public PurchasePersistenceAdapter(PurchaseJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Purchase> findByCompanyIdAndId(UUID companyId, UUID id) {
        return repository.findByCompanyIdAndId(companyId, id).map(PurchasePersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Purchase> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey) {
        return repository.findByCompanyIdAndIdempotencyKey(companyId, idempotencyKey)
                .map(PurchasePersistenceAdapter::toDomain);
    }

    @Override
    public List<Purchase> find(PurchaseQuery query) {
        return repository.findPurchasesDynamic(query.companyId(), query.status(), query.supplierId(),
                query.from() == null ? null : query.from().atStartOfDay().toInstant(ZoneOffset.UTC),
                query.to() == null ? null : query.to().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))
                .stream().map(PurchasePersistenceAdapter::toDomain).toList();
    }

    @Override
    public Purchase save(Purchase purchase) {
        return toDomain(repository.save(toEntity(purchase)));
    }

    private static Purchase toDomain(PurchaseJpaEntity entity) {
        return new Purchase(entity.getId(), entity.getCompanyId(), entity.getSupplierId(), entity.getStatus(),
                entity.getSubtotal(), entity.getTaxTotal(), entity.getTotal(), entity.getPaymentCondition(),
                entity.getDueDate(), entity.getEvidenceUrl(), entity.getIdempotencyKey(), entity.getCreatedAt(),
                entity.getConfirmedAt(),
                entity.getLines().stream().map(line -> toLineDomain(entity.getId(), line)).toList());
    }

    private static PurchaseLine toLineDomain(UUID purchaseId, PurchaseLineJpaEntity entity) {
        return new PurchaseLine(entity.getId(), purchaseId, entity.getProductId(), entity.getDescription(), entity.getQuantity(),
                entity.getUnitCost(), entity.getSubtotal(), entity.getTax(), entity.getTotal());
    }

    private static PurchaseJpaEntity toEntity(Purchase purchase) {
        PurchaseJpaEntity entity = new PurchaseJpaEntity();
        entity.setId(purchase.id());
        entity.setCompanyId(purchase.companyId());
        entity.setSupplierId(purchase.supplierId());
        entity.setStatus(purchase.status());
        entity.setSubtotal(purchase.subtotal());
        entity.setTaxTotal(purchase.taxTotal());
        entity.setTotal(purchase.total());
        entity.setPaymentCondition(purchase.paymentCondition());
        entity.setDueDate(purchase.dueDate());
        entity.setEvidenceUrl(purchase.evidenceUrl());
        entity.setIdempotencyKey(purchase.idempotencyKey());
        entity.setCreatedAt(purchase.createdAt());
        entity.setConfirmedAt(purchase.confirmedAt());
        entity.replaceLines(purchase.lines().stream().map(PurchasePersistenceAdapter::toLineEntity).toList());
        return entity;
    }

    private static PurchaseLineJpaEntity toLineEntity(PurchaseLine line) {
        PurchaseLineJpaEntity entity = new PurchaseLineJpaEntity();
        entity.setId(line.id());
        entity.setProductId(line.productId());
        entity.setDescription(line.description());
        entity.setQuantity(line.quantity());
        entity.setUnitCost(line.unitCost());
        entity.setSubtotal(line.subtotal());
        entity.setTax(line.tax());
        entity.setTotal(line.total());
        return entity;
    }
}
