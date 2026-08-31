package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.IssuerProfileResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.NumberingResolutionResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.QueryFiscalConfigurationUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IssuerProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.NumberingResolutionRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

public class QueryFiscalConfigurationService implements QueryFiscalConfigurationUseCase {

    private final IssuerProfileRepositoryPort issuerProfileRepository;
    private final NumberingResolutionRepositoryPort numberingResolutionRepository;

    public QueryFiscalConfigurationService(IssuerProfileRepositoryPort issuerProfileRepository,
            NumberingResolutionRepositoryPort numberingResolutionRepository) {
        this.issuerProfileRepository = Objects.requireNonNull(issuerProfileRepository);
        this.numberingResolutionRepository = Objects.requireNonNull(numberingResolutionRepository);
    }

    @Override
    public IssuerProfileResult findCurrentIssuer(UUID companyId) {
        return issuerProfileRepository.findActiveByCompanyId(companyId)
                .map(BillingResultMapper::toIssuerProfileResult)
                .orElseThrow(() -> new IllegalStateException(
                        "Debes configurar un emisor fiscal activo antes de emitir documentos fiscales."));
    }

    @Override
    public List<IssuerProfileResult> findIssuers(UUID companyId) {
        return issuerProfileRepository.findByCompanyId(companyId).stream()
                .map(BillingResultMapper::toIssuerProfileResult)
                .toList();
    }

    @Override
    public List<NumberingResolutionResult> findNumberingResolutions(UUID companyId,
            ElectronicDocumentType documentType, Boolean active) {
        return numberingResolutionRepository.findByCompanyId(companyId, documentType, active).stream()
                .map(resolution -> BillingResultMapper.toNumberingResolutionResult(resolution,
                        numberingResolutionRepository.usageCount(resolution)))
                .toList();
    }
}
