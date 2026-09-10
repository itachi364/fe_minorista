package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateMunicipalFiscalPackageCommand;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalSource;
import com.msvanegasg.facturaelectronica.accounting.domain.model.MunicipalFiscalRulePackage;
import com.msvanegasg.facturaelectronica.accounting.domain.model.MunicipalityReference;

public interface MunicipalFiscalPackageRepositoryPort {
    List<MunicipalFiscalRulePackage> findAll();
    Optional<MunicipalFiscalRulePackage> findById(UUID packageId);
    FiscalLegalSource findOrCreateSource(CreateMunicipalFiscalPackageCommand command, UUID sourceId);
    MunicipalFiscalRulePackage createDraft(UUID packageId, CreateMunicipalFiscalPackageCommand command,
            MunicipalityReference municipality, UUID legalSourceId, UUID importId, List<UUID> ruleIds);
    MunicipalFiscalRulePackage publish(UUID packageId, UUID userId);
    boolean hasPublishedOverlap(MunicipalFiscalRulePackage candidate);
    UUID saveImport(String fileName, String sha256, int rowCount, int packageCount, String status,
            String errorDetail, UUID userId);
}
