package com.msvanegasg.facturaelectronica.billing.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msvanegasg.facturaelectronica.billing.application.dto.IssuerProfileResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.NumberingResolutionResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ConfigureIssuerProfileUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.CreateNumberingResolutionUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.QueryFiscalConfigurationUseCase;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;
import com.msvanegasg.facturaelectronica.billing.exception.BillingExceptionHandler;

@ExtendWith(MockitoExtension.class)
class FiscalConfigurationControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ISSUER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID RESOLUTION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private MockMvc mockMvc;

    @Mock
    private ConfigureIssuerProfileUseCase configureIssuerProfileUseCase;
    @Mock
    private CreateNumberingResolutionUseCase createNumberingResolutionUseCase;
    @Mock
    private QueryFiscalConfigurationUseCase queryFiscalConfigurationUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new FiscalConfigurationController(configureIssuerProfileUseCase,
                createNumberingResolutionUseCase, queryFiscalConfigurationUseCase))
                .setControllerAdvice(new BillingExceptionHandler())
                .build();
    }

    @Test
    void configuresIssuer() throws Exception {
        when(configureIssuerProfileUseCase.configure(any())).thenReturn(issuer());

        mockMvc.perform(post("/api/v1/issuers")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "legalName": "ACME SAS",
                          "nit": "900123456",
                          "verificationDigit": "7",
                          "taxResponsibilities": ["O-13"],
                          "municipalityCode": "11001",
                          "address": "Calle 1 # 2-3"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ISSUER_ID.toString()))
                .andExpect(jsonPath("$.legalName").value("ACME SAS"));
    }

    @Test
    void findsCurrentIssuer() throws Exception {
        when(queryFiscalConfigurationUseCase.findCurrentIssuer(COMPANY_ID)).thenReturn(issuer());

        mockMvc.perform(get("/api/v1/issuers/current")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nit").value("900123456"));
    }

    @Test
    void listsIssuers() throws Exception {
        when(queryFiscalConfigurationUseCase.findIssuers(COMPANY_ID)).thenReturn(List.of(issuer()));

        mockMvc.perform(get("/api/v1/issuers")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ISSUER_ID.toString()))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void activatesIssuer() throws Exception {
        when(configureIssuerProfileUseCase.activate(COMPANY_ID, ISSUER_ID)).thenReturn(issuer());

        mockMvc.perform(put("/api/v1/issuers/{issuerId}/activate", ISSUER_ID)
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createsNumberingResolution() throws Exception {
        when(createNumberingResolutionUseCase.create(any())).thenReturn(resolution());

        mockMvc.perform(post("/api/v1/numbering-resolutions")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "documentType": "ELECTRONIC_POS",
                          "resolutionNumber": "18760000001",
                          "prefix": "POS",
                          "fromNumber": 100,
                          "toNumber": 200,
                          "validFrom": "2026-01-01",
                          "validTo": "2026-12-31",
                          "environment": "TEST"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentNumber").value(99));
    }

    @Test
    void listsNumberingResolutions() throws Exception {
        when(queryFiscalConfigurationUseCase.findNumberingResolutions(eq(COMPANY_ID),
                eq(ElectronicDocumentType.ELECTRONIC_POS), eq(true))).thenReturn(List.of(resolution()));

        mockMvc.perform(get("/api/v1/numbering-resolutions")
                .header("X-Company-Id", COMPANY_ID)
                .param("documentType", "ELECTRONIC_POS")
                .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].prefix").value("POS"));
    }

    @Test
    void deactivatesNumberingResolution() throws Exception {
        when(createNumberingResolutionUseCase.deactivate(COMPANY_ID, RESOLUTION_ID))
                .thenReturn(new NumberingResolutionResult(RESOLUTION_ID, COMPANY_ID,
                        ElectronicDocumentType.ELECTRONIC_POS, "18760000001", "POS", 100, 200, 99,
                        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), FiscalEnvironment.TEST, false));

        mockMvc.perform(put("/api/v1/numbering-resolutions/{resolutionId}/deactivate", RESOLUTION_ID)
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    private static IssuerProfileResult issuer() {
        return new IssuerProfileResult(ISSUER_ID, COMPANY_ID, "ACME SAS", "900123456", "7", List.of("O-13"),
                "11001", "Calle 1 # 2-3", true);
    }

    private static NumberingResolutionResult resolution() {
        return new NumberingResolutionResult(RESOLUTION_ID, COMPANY_ID, ElectronicDocumentType.ELECTRONIC_POS,
                "18760000001", "POS", 100, 200, 99, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                FiscalEnvironment.TEST, true);
    }
}
