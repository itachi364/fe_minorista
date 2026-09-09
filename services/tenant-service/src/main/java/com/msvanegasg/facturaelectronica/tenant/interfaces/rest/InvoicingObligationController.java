package com.msvanegasg.facturaelectronica.tenant.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.tenant.application.dto.InvoicingObligationCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.InvoicingObligationResult;
import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageInvoicingObligationUseCase;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.InvoicingObligationRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/invoicing-obligation")
public class InvoicingObligationController {
    private final ManageInvoicingObligationUseCase useCase;

    public InvoicingObligationController(ManageInvoicingObligationUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public InvoicingObligationResult current(@PathVariable UUID companyId) {
        return useCase.current(companyId);
    }

    @GetMapping("/history")
    public List<InvoicingObligationResult> history(@PathVariable UUID companyId) {
        return useCase.history(companyId);
    }

    @PutMapping
    public InvoicingObligationResult evaluate(@PathVariable UUID companyId,
            @RequestHeader(name = "X-User-Id", required = false) UUID userId,
            @Valid @RequestBody InvoicingObligationRequest request) {
        return useCase.evaluate(companyId, toCommand(request, userId));
    }

    private static InvoicingObligationCommand toCommand(InvoicingObligationRequest value, UUID userId) {
        return new InvoicingObligationCommand(value.personType(), value.taxRegime(), value.rutGeneratedAt(),
                value.rutResponsibilityCodes(), value.ciiuCodes(), value.economicOperationTypes(), value.customsUser(),
                value.establishmentCount(), value.exploitsIntangibles(), value.onlyExcludedOrUntaxedOperations(),
                value.previousYearGrossActivityIncome(), value.currentYearGrossActivityIncome(),
                value.previousYearTaxedActivityFinancialOperations(), value.currentYearTaxedActivityFinancialOperations(),
                value.largestPreviousYearTaxedContract(), value.largestCurrentYearTaxedContract(),
                value.largestSameCustomerAggregate(), value.voluntaryElectronicInvoicer(),
                value.specialExceptionType(), value.specialExceptionScope(), value.rutAssetId(), userId);
    }
}
