package com.msvanegasg.facturaelectronica.accounting.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateWithholdingRuleCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageFiscalCatalogUseCase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalParameter;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingRule;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.FiscalParameterResponse;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.WithholdingRuleRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.WithholdingRuleResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/fiscal-catalog")
public class FiscalCatalogController {
    private static final String COMPANY_HEADER = "X-Company-Id";
    private final ManageFiscalCatalogUseCase useCase;

    public FiscalCatalogController(ManageFiscalCatalogUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/parameters")
    public List<FiscalParameterResponse> parameters() {
        return useCase.parameters().stream().map(FiscalCatalogController::toResponse).toList();
    }

    @GetMapping("/rules")
    public List<WithholdingRuleResponse> rules(
            @RequestHeader(value = COMPANY_HEADER, required = false) UUID companyId,
            @RequestParam(required = false) Boolean active) {
        return useCase.rules(companyId, active).stream().map(FiscalCatalogController::toResponse).toList();
    }

    @PostMapping("/rules")
    public ResponseEntity<WithholdingRuleResponse> create(
            @RequestHeader(value = COMPANY_HEADER, required = false) UUID companyId,
            @Valid @RequestBody WithholdingRuleRequest request) {
        CreateWithholdingRuleCommand command = new CreateWithholdingRuleCommand(companyId, request.ruleSetVersion(),
                request.operationType(), request.conceptCode(), request.withholdingType(), request.rate(),
                request.thresholdUnit(), request.thresholdValue(), request.thresholdOperator(),
                request.calculationBase(), request.thresholdTreatment(), request.decision(),
                request.requiresCompanyWithholdingAgent(), request.requiresCompanyVatResponsible(),
                request.requiresCompanyVatWithholdingAgent(), request.requiresCompanyIcaWithholdingAgent(),
                request.requiredThirdPartyTaxRegime(), request.requiredThirdPartyResponsibility(),
                request.municipalityCode(), request.ciiuCode(), request.validFrom(), request.validTo(),
                request.priority(), request.specificity(), request.legalReference(), request.sourceUrl(),
                request.published(), request.targetThirdPartyId(), request.evidenceReference(),
                request.triggerMoment(), request.accumulationScope(), request.requiredThirdPartyPersonType(),
                request.requiredThirdPartyTaxResidency(), request.requiredThirdPartyIncomeTaxStatus(),
                request.requiredThirdPartySelfWithholdingScope());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(useCase.create(command)));
    }

    @PutMapping("/rules/{ruleId}/deactivate")
    public WithholdingRuleResponse deactivate(
            @RequestHeader(value = COMPANY_HEADER, required = false) UUID companyId,
            @PathVariable UUID ruleId) {
        return toResponse(useCase.deactivate(companyId, ruleId));
    }

    private static FiscalParameterResponse toResponse(FiscalParameter parameter) {
        return new FiscalParameterResponse(parameter.id(), parameter.code(), parameter.version(), parameter.value(),
                parameter.validFrom(), parameter.validTo(), parameter.legalReference(), parameter.sourceUrl(),
                parameter.published());
    }

    private static WithholdingRuleResponse toResponse(WithholdingRule rule) {
        return new WithholdingRuleResponse(rule.id(), rule.companyId(), rule.ruleSetVersion(), rule.operationType(),
                rule.conceptCode(), rule.withholdingType(), rule.rate(), rule.thresholdUnit(), rule.thresholdValue(),
                rule.thresholdOperator(), rule.calculationBase(), rule.thresholdTreatment(), rule.decision(),
                rule.requiresCompanyWithholdingAgent(), rule.requiresCompanyVatResponsible(),
                rule.requiresCompanyVatWithholdingAgent(), rule.requiresCompanyIcaWithholdingAgent(),
                rule.requiredThirdPartyTaxRegime(), rule.requiredThirdPartyResponsibility(), rule.municipalityCode(),
                rule.ciiuCode(), rule.validFrom(), rule.validTo(), rule.priority(), rule.specificity(),
                rule.legalReference(), rule.sourceUrl(), rule.published(), rule.active(), rule.targetThirdPartyId(),
                rule.evidenceReference(), rule.triggerMoment(), rule.accumulationScope(),
                rule.requiredThirdPartyPersonType(), rule.requiredThirdPartyTaxResidency(),
                rule.requiredThirdPartyIncomeTaxStatus(), rule.requiredThirdPartySelfWithholdingScope());
    }
}
