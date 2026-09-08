package com.msvanegasg.facturaelectronica.tenant.interfaces.rest;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyTaxProfileCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyTaxProfileResult;
import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyTaxProfileUseCase;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyTaxProfileRequest;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyTaxProfileResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/tax-profile")
public class CompanyTaxProfileController {
    private final ManageCompanyTaxProfileUseCase useCase;

    public CompanyTaxProfileController(ManageCompanyTaxProfileUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public CompanyTaxProfileResponse find(@PathVariable UUID companyId) {
        return toResponse(useCase.findByCompanyId(companyId));
    }

    @PutMapping
    public CompanyTaxProfileResponse update(@PathVariable UUID companyId,
            @RequestHeader(name = "X-User-Id", required = false) UUID userId,
            @Valid @RequestBody CompanyTaxProfileRequest request) {
        return toResponse(useCase.update(companyId, new CompanyTaxProfileCommand(request.companySize(),
                request.financialReportingGroup(), request.taxRegime(), request.rutResponsibilities(),
                request.vatResponsible(), request.withholdingAgent(), request.vatWithholdingAgent(),
                request.icaWithholdingAgent(), request.largeTaxpayer(), request.selfWithholding(),
                request.simpleRegime(), request.icaMunicipalityCode(), request.ciiuCodes(), userId)));
    }

    private static CompanyTaxProfileResponse toResponse(CompanyTaxProfileResult result) {
        return new CompanyTaxProfileResponse(result.companyId(), result.companySize(),
                result.financialReportingGroup(), result.taxRegime(), result.rutResponsibilities(),
                result.vatResponsible(), result.withholdingAgent(), result.vatWithholdingAgent(),
                result.icaWithholdingAgent(), result.largeTaxpayer(), result.selfWithholding(),
                result.simpleRegime(), result.icaMunicipalityCode(), result.ciiuCodes(), result.updatedBy(),
                result.updatedAt());
    }
}
