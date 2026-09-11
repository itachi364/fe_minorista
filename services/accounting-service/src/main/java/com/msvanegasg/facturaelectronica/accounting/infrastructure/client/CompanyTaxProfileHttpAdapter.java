package com.msvanegasg.facturaelectronica.accounting.infrastructure.client;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.CompanyTaxProfilePort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.CompanyTaxProfile;

@Component
public class CompanyTaxProfileHttpAdapter implements CompanyTaxProfilePort {
    private final RestClient restClient;
    private final String tenantBaseUrl;

    public CompanyTaxProfileHttpAdapter(RestClient.Builder restClientBuilder,
            @Value("${services.tenant.base-url:}") String tenantBaseUrl) {
        this.restClient = restClientBuilder.build();
        this.tenantBaseUrl = tenantBaseUrl;
    }

    @Override
    public Optional<CompanyTaxProfile> findByCompanyId(UUID companyId) {
        return find(companyId, null);
    }

    @Override
    public Optional<CompanyTaxProfile> findByCompanyIdAndDate(UUID companyId, LocalDate effectiveOn) {
        return find(companyId, effectiveOn);
    }

    private Optional<CompanyTaxProfile> find(UUID companyId, LocalDate effectiveOn) {
        if (tenantBaseUrl == null || tenantBaseUrl.isBlank()) {
            return Optional.empty();
        }
        try {
            CompanyTaxProfileResponse response = restClient.get()
                    .uri(tenantBaseUrl + "/api/v1/companies/" + companyId + "/tax-profile"
                            + (effectiveOn == null ? "" : "?effectiveOn=" + effectiveOn))
                    .retrieve()
                    .body(CompanyTaxProfileResponse.class);
            if (response == null) {
                return Optional.empty();
            }
            return Optional.of(new CompanyTaxProfile(response.taxRegime(), safe(response.rutResponsibilities()),
                    response.vatResponsible(), response.withholdingAgent(), response.largeTaxpayer(),
                    response.selfWithholding(), response.simpleRegime(), response.icaMunicipalityCode(),
                    safe(response.ciiuCodes()), response.vatWithholdingAgent(), response.icaWithholdingAgent(),
                    response.taxResidency(), response.incomeTaxStatus(), safe(response.selfWithholdingScopes()),
                    response.fiscalEvidenceReference()));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static Set<String> safe(Set<String> values) {
        return values == null ? Set.of() : values;
    }

    private record CompanyTaxProfileResponse(
            String taxRegime,
            Set<String> rutResponsibilities,
            boolean vatResponsible,
            boolean withholdingAgent,
            boolean vatWithholdingAgent,
            boolean icaWithholdingAgent,
            boolean largeTaxpayer,
            boolean selfWithholding,
            boolean simpleRegime,
            String icaMunicipalityCode,
            Set<String> ciiuCodes,
            String taxResidency,
            String incomeTaxStatus,
            Set<String> selfWithholdingScopes,
            String fiscalEvidenceReference) {
    }
}
