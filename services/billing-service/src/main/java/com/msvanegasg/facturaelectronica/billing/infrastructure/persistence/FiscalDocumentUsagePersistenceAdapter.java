package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalDocumentUsagePort;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.FiscalNoteJpaRepository;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository.SaleJpaRepository;

@Component
public class FiscalDocumentUsagePersistenceAdapter implements FiscalDocumentUsagePort {

    private final SaleJpaRepository saleRepository;
    private final FiscalNoteJpaRepository fiscalNoteRepository;

    public FiscalDocumentUsagePersistenceAdapter(SaleJpaRepository saleRepository,
            FiscalNoteJpaRepository fiscalNoteRepository) {
        this.saleRepository = saleRepository;
        this.fiscalNoteRepository = fiscalNoteRepository;
    }

    @Override
    public long countIssuedDocuments(UUID companyId, Instant fromInclusive, Instant toExclusive) {
        return saleRepository.countIssuedElectronicDocuments(companyId, fromInclusive, toExclusive)
                + fiscalNoteRepository.countIssuedFiscalNotes(companyId, fromInclusive, toExclusive);
    }
}
