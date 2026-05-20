package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.enums.Estado;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.PurchaseRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseLine;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseStatus;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.PurchaseJpaEntity;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.PurchaseLineJpaEntity;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository.PurchaseJpaRepository;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository.PurchaseLineJpaRepository;

@Component
public class PurchasePersistenceAdapter implements PurchaseRepositoryPort {

    private final PurchaseJpaRepository purchaseRepository;
    private final PurchaseLineJpaRepository purchaseLineRepository;

    public PurchasePersistenceAdapter(PurchaseJpaRepository purchaseRepository,
            PurchaseLineJpaRepository purchaseLineRepository) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseLineRepository = purchaseLineRepository;
    }

    @Override
    public Purchase save(Purchase purchase) {
        PurchaseJpaEntity saved = purchaseRepository.save(toEntity(purchase));
        if (purchase.id() != null) {
            purchaseLineRepository.findByCompraIdCompra(purchase.id())
                    .forEach(detail -> {
                        detail.setActivo(false);
                        purchaseLineRepository.save(detail);
                    });
        }
        purchase.lines().forEach(line -> purchaseLineRepository.save(toEntity(line, saved)));
        return toDomain(saved, purchaseLineRepository.findByCompraIdCompra(saved.getIdCompra()));
    }

    @Override
    public Optional<Purchase> findById(Long purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .map(purchase -> toDomain(purchase, purchaseLineRepository.findByCompraIdCompra(purchaseId)));
    }

    @Override
    public List<Purchase> findActive() {
        return purchaseRepository.findByActivoTrue().stream()
                .map(purchase -> toDomain(purchase,
                        purchaseLineRepository.findByCompraIdCompra(purchase.getIdCompra())))
                .toList();
    }

    @Override
    public boolean existsById(Long purchaseId) {
        return purchaseRepository.existsById(purchaseId);
    }

    private static Purchase toDomain(PurchaseJpaEntity purchase, List<PurchaseLineJpaEntity> details) {
        return Purchase.restore(
                purchase.getIdCompra(),
                purchase.getIdProveedor(),
                purchase.getFecha(),
                purchase.getSubtotal(),
                purchase.getIvaTotal(),
                purchase.getTotal(),
                purchase.getUrlEvidencia(),
                toDomainStatus(purchase.getEstado()),
                Boolean.TRUE.equals(purchase.getActivo()),
                details.stream().map(PurchasePersistenceAdapter::toDomainLine).toList());
    }

    private static PurchaseLine toDomainLine(PurchaseLineJpaEntity detail) {
        return PurchaseLine.restore(
                detail.getIdDetalle(),
                detail.getProducto(),
                detail.getCantidad(),
                detail.getPrecioUnitario(),
                detail.getSubtotal(),
                detail.getIva(),
                detail.getTotalLinea(),
                Boolean.TRUE.equals(detail.getActivo()));
    }

    private static PurchaseJpaEntity toEntity(Purchase purchase) {
        return PurchaseJpaEntity.builder()
                .idCompra(purchase.id())
                .idProveedor(purchase.supplierId())
                .fecha(purchase.date())
                .subtotal(purchase.subtotal())
                .ivaTotal(purchase.taxTotal())
                .total(purchase.total())
                .urlEvidencia(purchase.evidenceUrl())
                .estado(toJpaStatus(purchase.status()))
                .activo(purchase.active())
                .build();
    }

    private static PurchaseLineJpaEntity toEntity(PurchaseLine line, PurchaseJpaEntity purchase) {
        return PurchaseLineJpaEntity.builder()
                .idDetalle(line.id())
                .compra(purchase)
                .producto(line.productId())
                .cantidad(line.quantity())
                .precioUnitario(line.unitPrice())
                .subtotal(line.subtotal())
                .iva(line.tax())
                .totalLinea(line.lineTotal())
                .activo(line.active())
                .build();
    }

    private static PurchaseStatus toDomainStatus(Estado status) {
        return switch (status) {
            case PENDIENTE -> PurchaseStatus.PENDING;
            case PROCESADO -> PurchaseStatus.PROCESSED;
            case ANULADO -> PurchaseStatus.CANCELLED;
        };
    }

    private static Estado toJpaStatus(PurchaseStatus status) {
        return switch (status) {
            case PENDING -> Estado.PENDIENTE;
            case PROCESSED -> Estado.PROCESADO;
            case CANCELLED -> Estado.ANULADO;
        };
    }
}
