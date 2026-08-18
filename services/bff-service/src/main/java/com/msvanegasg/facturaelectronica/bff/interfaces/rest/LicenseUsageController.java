package com.msvanegasg.facturaelectronica.bff.interfaces.rest;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.msvanegasg.facturaelectronica.bff.infrastructure.client.BffAccessDeniedException;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffProperties;

@RestController
public class LicenseUsageController {

    private final RestClient tenantClient;
    private final RestClient identityClient;
    private final RestClient billingClient;

    public LicenseUsageController(RestClient.Builder builder, BffProperties properties) {
        this.tenantClient = builder.clone().baseUrl(properties.tenantUrl()).build();
        this.identityClient = builder.clone().baseUrl(properties.identityUrl()).build();
        this.billingClient = builder.clone().baseUrl(properties.billingUrl()).build();
    }

    @GetMapping("/api/v1/platform/licenses/usage")
    public LicenseUsageResponse usage(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestParam UUID companyId) {
        requireRoot(authorization);
        CompanyResponse company = tenantClient.get()
                .uri("/api/v1/companies/{companyId}", companyId)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(CompanyResponse.class);
        CompanyLicenseResponse license = tenantClient.get()
                .uri("/api/v1/companies/{companyId}/license", companyId)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(CompanyLicenseResponse.class);
        UserResponse[] users = identityClient.get()
                .uri("/api/v1/companies/{companyId}/users", companyId)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .header("X-Company-Id", companyId.toString())
                .retrieve()
                .body(UserResponse[].class);
        ElectronicDocumentResponse[] documents = billingClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/reports/electronic-documents")
                        .queryParam("from", YearMonth.now().atDay(1))
                        .queryParam("to", LocalDate.now())
                        .build())
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .header("X-Company-Id", companyId.toString())
                .retrieve()
                .body(ElectronicDocumentResponse[].class);

        return new LicenseUsageResponse(
                companyId,
                company == null ? "" : company.legalName(),
                license == null ? "" : license.status(),
                license == null ? null : license.validFrom(),
                license == null ? null : license.validTo(),
                license == null || license.enabledModules() == null ? Set.of() : license.enabledModules(),
                activeUsers(users),
                license == null ? null : license.maxUsers(),
                documents == null ? 0 : documents.length,
                license == null ? null : license.maxMonthlyDocuments());
    }

    private void requireRoot(String authorization) {
        try {
            identityClient.get()
                    .uri("/api/v1/platform/permissions")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException exception) {
            throw new BffAccessDeniedException("ROOT is required for license usage dashboard");
        }
    }

    private static int activeUsers(UserResponse[] users) {
        if (users == null) {
            return 0;
        }
        int total = 0;
        for (UserResponse user : users) {
            if ("ACTIVE".equals(user.status())) {
                total++;
            }
        }
        return total;
    }

    public record LicenseUsageResponse(
            UUID companyId,
            String companyName,
            String licenseStatus,
            LocalDate validFrom,
            LocalDate validTo,
            Set<String> enabledModules,
            int activeUsers,
            Integer maxUsers,
            int monthlyDocuments,
            Integer maxMonthlyDocuments) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record CompanyResponse(UUID id, String legalName) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record CompanyLicenseResponse(
            UUID companyId,
            String status,
            LocalDate validFrom,
            LocalDate validTo,
            Integer maxUsers,
            Integer maxMonthlyDocuments,
            Set<String> enabledModules) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record UserResponse(UUID id, String status) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ElectronicDocumentResponse(UUID id) {
    }
}
