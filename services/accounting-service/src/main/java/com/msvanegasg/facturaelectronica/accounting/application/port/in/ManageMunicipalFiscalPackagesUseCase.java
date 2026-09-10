package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateMunicipalFiscalPackageCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalCsvValidationResult;
import com.msvanegasg.facturaelectronica.accounting.domain.model.MunicipalFiscalRulePackage;

public interface ManageMunicipalFiscalPackagesUseCase {
    List<MunicipalFiscalRulePackage> findAll();
    MunicipalFiscalRulePackage createDraft(CreateMunicipalFiscalPackageCommand command);
    FiscalCsvValidationResult validateCsv(String fileName, byte[] content);
    List<MunicipalFiscalRulePackage> importCsv(String fileName, byte[] content, UUID userId);
    MunicipalFiscalRulePackage publish(UUID packageId, UUID userId);
}
