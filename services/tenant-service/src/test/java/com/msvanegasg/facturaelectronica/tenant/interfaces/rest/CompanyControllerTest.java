package com.msvanegasg.facturaelectronica.tenant.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyResult;
import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyUseCase;
import com.msvanegasg.facturaelectronica.tenant.application.usecase.CompanyAlreadyExistsException;
import com.msvanegasg.facturaelectronica.tenant.application.usecase.CompanyNotFoundException;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyStatus;
import com.msvanegasg.facturaelectronica.tenant.exception.TenantExceptionHandler;
import com.msvanegasg.facturaelectronica.tenant.observability.CorrelationId;
import com.msvanegasg.facturaelectronica.tenant.observability.CorrelationIdFilter;

@ExtendWith(MockitoExtension.class)
class CompanyControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID IDENTIFICATION_TYPE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    private MockMvc mockMvc;

    @Mock
    private ManageCompanyUseCase manageCompanyUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CompanyController(manageCompanyUseCase))
                .setControllerAdvice(new TenantExceptionHandler())
                .addFilters(new CorrelationIdFilter())
                .build();
    }

    @Test
    void createsCompany() throws Exception {
        when(manageCompanyUseCase.create(any())).thenReturn(result(CompanyStatus.ACTIVE));

        mockMvc.perform(post("/api/v1/companies")
                .header(CorrelationId.HEADER_NAME, "corr-tenant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(companyJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string(CorrelationId.HEADER_NAME, "corr-tenant"))
                .andExpect(jsonPath("$.id").value(COMPANY_ID.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void findsCompany() throws Exception {
        when(manageCompanyUseCase.findById(eq(COMPANY_ID))).thenReturn(result(CompanyStatus.ACTIVE));

        mockMvc.perform(get("/api/v1/companies/{companyId}", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.legalName").value("Mi Empresa SAS"));
    }

    @Test
    void suspendsCompany() throws Exception {
        when(manageCompanyUseCase.suspend(eq(COMPANY_ID))).thenReturn(result(CompanyStatus.SUSPENDED));

        mockMvc.perform(put("/api/v1/companies/{companyId}/suspend", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    void returnsNotFoundForMissingCompany() throws Exception {
        when(manageCompanyUseCase.findById(eq(COMPANY_ID))).thenThrow(new CompanyNotFoundException(COMPANY_ID));

        mockMvc.perform(get("/api/v1/companies/{companyId}", COMPANY_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void returnsConflictForDuplicatedCompany() throws Exception {
        when(manageCompanyUseCase.create(any())).thenThrow(new CompanyAlreadyExistsException("900123456"));

        mockMvc.perform(post("/api/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(companyJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void validatesRequest() throws Exception {
        mockMvc.perform(post("/api/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private static CompanyResult result(CompanyStatus status) {
        return new CompanyResult(COMPANY_ID, "Mi Empresa SAS", "Mi Tienda", IDENTIFICATION_TYPE_ID, "900123456", "7",
                "admin@example.com", status, NOW, NOW);
    }

    private static String companyJson() {
        return """
                {
                  "legalName": "Mi Empresa SAS",
                  "tradeName": "Mi Tienda",
                  "identificationTypeId": "22222222-2222-2222-2222-222222222222",
                  "identificationNumber": "900123456",
                  "verificationDigit": "7",
                  "email": "admin@example.com"
                }
                """;
    }
}
