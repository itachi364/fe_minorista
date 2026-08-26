package com.msvanegasg.facturaelectronica.billing.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.billing.application.port.in.ConfigureIssuerProfileUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.CreateNumberingResolutionUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ManageCompanyFiscalPolicyUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.QueryFiscalConfigurationUseCase;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.CompanyFiscalPolicyRequest;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.CompanyFiscalPolicyResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.IssuerProfileRequest;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.IssuerProfileResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.NumberingResolutionRequest;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.NumberingResolutionResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class FiscalConfigurationController {

    private static final String COMPANY_HEADER = "X-Company-Id";

    private final ConfigureIssuerProfileUseCase configureIssuerProfileUseCase;
    private final CreateNumberingResolutionUseCase createNumberingResolutionUseCase;
    private final QueryFiscalConfigurationUseCase queryFiscalConfigurationUseCase;
    private final ManageCompanyFiscalPolicyUseCase manageCompanyFiscalPolicyUseCase;

    public FiscalConfigurationController(ConfigureIssuerProfileUseCase configureIssuerProfileUseCase,
            CreateNumberingResolutionUseCase createNumberingResolutionUseCase,
            QueryFiscalConfigurationUseCase queryFiscalConfigurationUseCase,
            ManageCompanyFiscalPolicyUseCase manageCompanyFiscalPolicyUseCase) {
        this.configureIssuerProfileUseCase = configureIssuerProfileUseCase;
        this.createNumberingResolutionUseCase = createNumberingResolutionUseCase;
        this.queryFiscalConfigurationUseCase = queryFiscalConfigurationUseCase;
        this.manageCompanyFiscalPolicyUseCase = manageCompanyFiscalPolicyUseCase;
    }

    @GetMapping("/fiscal-policy")
    public CompanyFiscalPolicyResponse findFiscalPolicy(@RequestHeader(COMPANY_HEADER) UUID companyId) {
        return BillingRestMapper.toResponse(manageCompanyFiscalPolicyUseCase.findByCompanyId(companyId));
    }

    @PutMapping("/fiscal-policy")
    public CompanyFiscalPolicyResponse configureFiscalPolicy(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody CompanyFiscalPolicyRequest request) {
        return BillingRestMapper.toResponse(manageCompanyFiscalPolicyUseCase.configure(
                BillingRestMapper.toCommand(companyId, request)));
    }

    @PostMapping("/issuers")
    public ResponseEntity<IssuerProfileResponse> configureIssuer(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody IssuerProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BillingRestMapper.toResponse(
                        configureIssuerProfileUseCase.configure(BillingRestMapper.toCommand(companyId, request))));
    }

    @GetMapping("/issuers/current")
    public IssuerProfileResponse findCurrentIssuer(@RequestHeader(COMPANY_HEADER) UUID companyId) {
        return BillingRestMapper.toResponse(queryFiscalConfigurationUseCase.findCurrentIssuer(companyId));
    }

    @GetMapping("/issuers")
    public List<IssuerProfileResponse> findIssuers(@RequestHeader(COMPANY_HEADER) UUID companyId) {
        return queryFiscalConfigurationUseCase.findIssuers(companyId).stream()
                .map(BillingRestMapper::toResponse)
                .toList();
    }

    @PutMapping("/issuers/{issuerId}/activate")
    public IssuerProfileResponse activateIssuer(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @PathVariable UUID issuerId) {
        return BillingRestMapper.toResponse(configureIssuerProfileUseCase.activate(companyId, issuerId));
    }

    @PutMapping("/issuers/{issuerId}/deactivate")
    public IssuerProfileResponse deactivateIssuer(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @PathVariable UUID issuerId) {
        return BillingRestMapper.toResponse(configureIssuerProfileUseCase.deactivate(companyId, issuerId));
    }

    @PostMapping("/numbering-resolutions")
    public ResponseEntity<NumberingResolutionResponse> createNumberingResolution(
            @RequestHeader(COMPANY_HEADER) UUID companyId, @Valid @RequestBody NumberingResolutionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BillingRestMapper.toResponse(createNumberingResolutionUseCase.create(
                        BillingRestMapper.toCommand(companyId, request))));
    }

    @GetMapping("/numbering-resolutions")
    public List<NumberingResolutionResponse> findNumberingResolutions(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam(required = false) ElectronicDocumentType documentType,
            @RequestParam(required = false) Boolean active) {
        return queryFiscalConfigurationUseCase.findNumberingResolutions(companyId, documentType, active).stream()
                .map(BillingRestMapper::toResponse)
                .toList();
    }

    @PutMapping("/numbering-resolutions/{resolutionId}/activate")
    public NumberingResolutionResponse activateNumberingResolution(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @PathVariable UUID resolutionId) {
        return BillingRestMapper.toResponse(createNumberingResolutionUseCase.activate(companyId, resolutionId));
    }

    @PutMapping("/numbering-resolutions/{resolutionId}/deactivate")
    public NumberingResolutionResponse deactivateNumberingResolution(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @PathVariable UUID resolutionId) {
        return BillingRestMapper.toResponse(createNumberingResolutionUseCase.deactivate(companyId, resolutionId));
    }
}
