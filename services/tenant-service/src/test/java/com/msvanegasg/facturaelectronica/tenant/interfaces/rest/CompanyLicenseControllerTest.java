package com.msvanegasg.facturaelectronica.tenant.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyLicenseResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyLicenseValidationResult;
import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyLicenseUseCase;
import com.msvanegasg.facturaelectronica.tenant.application.usecase.CompanyLicenseNotFoundException;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicenseStatus;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseAction;
import com.msvanegasg.facturaelectronica.tenant.exception.TenantExceptionHandler;
import com.msvanegasg.facturaelectronica.tenant.observability.CorrelationIdFilter;

@ExtendWith(MockitoExtension.class)
class CompanyLicenseControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID LICENSE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    private MockMvc mockMvc;

    @Mock
    private ManageCompanyLicenseUseCase manageCompanyLicenseUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CompanyLicenseController(manageCompanyLicenseUseCase))
                .setControllerAdvice(new TenantExceptionHandler())
                .addFilters(new CorrelationIdFilter())
                .build();
    }

    @Test
    void savesCompanyLicense() throws Exception {
        when(manageCompanyLicenseUseCase.save(eq(COMPANY_ID), any())).thenReturn(result(CompanyLicenseStatus.ACTIVE));

        mockMvc.perform(post("/api/v1/companies/{companyId}/license", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(licenseJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyId").value(COMPANY_ID.toString()))
                .andExpect(jsonPath("$.planCode").value("SMALL_BUSINESS"));
    }

    @Test
    void suspendsCompanyLicense() throws Exception {
        when(manageCompanyLicenseUseCase.suspend(eq(COMPANY_ID))).thenReturn(result(CompanyLicenseStatus.SUSPENDED));

        mockMvc.perform(put("/api/v1/companies/{companyId}/license/suspend", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    void validatesBlockedLicense() throws Exception {
        when(manageCompanyLicenseUseCase.validate(eq(COMPANY_ID), eq(LicenseAction.ISSUE_FISCAL_DOCUMENT)))
                .thenReturn(new CompanyLicenseValidationResult(COMPANY_ID, LicenseAction.ISSUE_FISCAL_DOCUMENT,
                        false, CompanyLicenseStatus.SUSPENDED, "LICENSE_SUSPENDED",
                        "La licencia de la empresa esta suspendida."));

        mockMvc.perform(get("/api/v1/companies/{companyId}/license/validation", COMPANY_ID)
                .queryParam("action", "ISSUE_FISCAL_DOCUMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.reasonCode").value("LICENSE_SUSPENDED"));
    }

    @Test
    void returnsNotFoundForMissingLicense() throws Exception {
        when(manageCompanyLicenseUseCase.findByCompanyId(eq(COMPANY_ID)))
                .thenThrow(new CompanyLicenseNotFoundException(COMPANY_ID));

        mockMvc.perform(get("/api/v1/companies/{companyId}/license", COMPANY_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void validatesRequest() throws Exception {
        mockMvc.perform(post("/api/v1/companies/{companyId}/license", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private static CompanyLicenseResult result(CompanyLicenseStatus status) {
        return new CompanyLicenseResult(LICENSE_ID, COMPANY_ID, "SMALL_BUSINESS", status,
                LocalDate.parse("2026-05-01"), LocalDate.parse("2027-05-01"), 5, 1000, NOW, NOW);
    }

    private static String licenseJson() {
        return """
                {
                  "planCode": "SMALL_BUSINESS",
                  "validFrom": "2026-05-01",
                  "validTo": "2027-05-01",
                  "maxUsers": 5,
                  "maxMonthlyDocuments": 1000
                }
                """;
    }
}
