package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicPosDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.QueryElectronicPosDocumentUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicPosDocumentRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicPosDocument;

public class QueryElectronicPosDocumentService implements QueryElectronicPosDocumentUseCase {

    private final ElectronicPosDocumentRepositoryPort repository;

    public QueryElectronicPosDocumentService(ElectronicPosDocumentRepositoryPort repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public ElectronicPosDocumentResult findById(UUID companyId, UUID documentId) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(documentId, "documentId is required");
        ElectronicPosDocument document = repository.findByCompanyIdAndDocumentId(companyId, documentId)
                .orElseThrow(() -> new IllegalStateException("electronic POS document was not found"));
        return toResult(document);
    }

    private static ElectronicPosDocumentResult toResult(ElectronicPosDocument document) {
        return new ElectronicPosDocumentResult(
                document.id(),
                document.companyId(),
                document.saleId(),
                document.buyerInformation().name(),
                document.buyerInformation().documentType(),
                document.buyerInformation().documentNumber(),
                document.prefix(),
                document.number(),
                document.cude(),
                document.subtotal(),
                document.taxTotal(),
                document.total(),
                document.status(),
                document.issueAt());
    }
}
