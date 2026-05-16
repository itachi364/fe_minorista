package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicPosDocumentRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.BuyerInformation;
import com.msvanegasg.facturaelectronica.billing.domain.model.CalculatedDocumentLine;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicPosDocument;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.BillingElectronicPosDocumentJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.BillingElectronicPosDocumentLineJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.BillingElectronicPosDocumentJpaRepository;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.BillingElectronicPosDocumentLineJpaRepository;

@Component
public class BillingElectronicPosDocumentPersistenceAdapter
        implements ElectronicPosDocumentRepositoryPort {

    private final BillingElectronicPosDocumentJpaRepository documentRepository;
    private final BillingElectronicPosDocumentLineJpaRepository lineRepository;

    public BillingElectronicPosDocumentPersistenceAdapter(BillingElectronicPosDocumentJpaRepository documentRepository,
            BillingElectronicPosDocumentLineJpaRepository lineRepository) {
        this.documentRepository = documentRepository;
        this.lineRepository = lineRepository;
    }

    @Override
    @Transactional
    public ElectronicPosDocument save(ElectronicPosDocument document) {
        BillingElectronicPosDocumentJpaEntity savedDocument = documentRepository.save(toEntity(document));
        lineRepository.deleteByDocumentId(savedDocument.getId());
        document.lines().forEach(line -> lineRepository.save(toEntity(line, savedDocument)));
        return toDomain(savedDocument, lineRepository.findByDocumentId(savedDocument.getId()));
    }

    @Override
    public Optional<ElectronicPosDocument> findByCompanyIdAndDocumentId(UUID companyId, UUID documentId) {
        return documentRepository.findByCompanyIdAndId(companyId, documentId)
                .map(document -> toDomain(document, lineRepository.findByDocumentId(document.getId())));
    }

    private static BillingElectronicPosDocumentJpaEntity toEntity(ElectronicPosDocument document) {
        return BillingElectronicPosDocumentJpaEntity.builder()
                .id(document.id())
                .companyId(document.companyId())
                .saleId(document.saleId())
                .buyerName(document.buyerInformation().name())
                .buyerDocumentType(document.buyerInformation().documentType())
                .buyerDocumentNumber(document.buyerInformation().documentNumber())
                .prefix(document.prefix())
                .documentNumber(document.number())
                .cude(document.cude())
                .subtotal(document.subtotal())
                .taxTotal(document.taxTotal())
                .total(document.total())
                .status(document.status())
                .issueAt(document.issueAt())
                .updatedAt(document.issueAt())
                .build();
    }

    private static BillingElectronicPosDocumentLineJpaEntity toEntity(
            CalculatedDocumentLine line,
            BillingElectronicPosDocumentJpaEntity document) {
        return BillingElectronicPosDocumentLineJpaEntity.builder()
                .id(UUID.randomUUID())
                .document(document)
                .productId(line.productId())
                .quantity(line.quantity())
                .unitPrice(line.unitPrice())
                .discountAmount(line.discountAmount())
                .taxCode(line.taxCode())
                .taxRate(line.taxRate())
                .grossAmount(line.grossAmount())
                .taxableAmount(line.taxableAmount())
                .taxAmount(line.taxAmount())
                .lineTotal(line.lineTotal())
                .build();
    }

    private static ElectronicPosDocument toDomain(
            BillingElectronicPosDocumentJpaEntity document,
            List<BillingElectronicPosDocumentLineJpaEntity> lines) {
        return ElectronicPosDocument.restore(
                document.getId(),
                document.getCompanyId(),
                document.getSaleId(),
                new BuyerInformation(
                        document.getBuyerName(),
                        document.getBuyerDocumentType(),
                        document.getBuyerDocumentNumber()),
                document.getPrefix(),
                document.getDocumentNumber(),
                document.getCude(),
                document.getSubtotal(),
                document.getTaxTotal(),
                document.getTotal(),
                lines.stream().map(BillingElectronicPosDocumentPersistenceAdapter::toDomainLine).toList(),
                document.getStatus(),
                document.getIssueAt());
    }

    private static CalculatedDocumentLine toDomainLine(BillingElectronicPosDocumentLineJpaEntity line) {
        return new CalculatedDocumentLine(
                line.getProductId(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getDiscountAmount(),
                line.getTaxCode(),
                line.getTaxRate(),
                line.getGrossAmount(),
                line.getTaxableAmount(),
                line.getTaxAmount(),
                line.getLineTotal());
    }

}
