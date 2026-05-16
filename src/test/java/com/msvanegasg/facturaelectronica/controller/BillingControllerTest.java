package com.msvanegasg.facturaelectronica.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
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

import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicPosDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.IssuerProfileResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.NumberingResolutionResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SubmitElectronicPosDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ConfigureIssuerProfileUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.CreateNumberingResolutionUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.IssueElectronicPosUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.QueryElectronicPosDocumentUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.in.SubmitElectronicPosDocumentUseCase;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionStatus;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.BillingController;
import com.msvanegasg.facturaelectronica.exception.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
class BillingControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID DOCUMENT_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private MockMvc mockMvc;

    @Mock
    private ConfigureIssuerProfileUseCase configureIssuerProfileUseCase;

    @Mock
    private CreateNumberingResolutionUseCase createNumberingResolutionUseCase;

    @Mock
    private IssueElectronicPosUseCase issueElectronicPosUseCase;

    @Mock
    private QueryElectronicPosDocumentUseCase queryElectronicPosDocumentUseCase;

    @Mock
    private SubmitElectronicPosDocumentUseCase submitElectronicPosDocumentUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new BillingController(
                        configureIssuerProfileUseCase,
                        createNumberingResolutionUseCase,
                        issueElectronicPosUseCase,
                        queryElectronicPosDocumentUseCase,
                        submitElectronicPosDocumentUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void configureIssuerReturnsCreatedIssuer() throws Exception {
        when(configureIssuerProfileUseCase.configure(any())).thenReturn(new IssuerProfileResult(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                COMPANY_ID,
                "Mi Tienda SAS",
                "900123456",
                "7",
                List.of("R-99-PN"),
                "11001",
                "Calle 1",
                true));

        mockMvc.perform(post("/api/v1/issuers")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "legalName":"Mi Tienda SAS",
                          "nit":"900123456",
                          "verificationDigit":"7",
                          "taxResponsibilities":["R-99-PN"],
                          "municipalityCode":"11001",
                          "address":"Calle 1"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyId").value(COMPANY_ID.toString()))
                .andExpect(jsonPath("$.legalName").value("Mi Tienda SAS"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createNumberingResolutionReturnsCreatedResolution() throws Exception {
        when(createNumberingResolutionUseCase.create(any())).thenReturn(new NumberingResolutionResult(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_POS,
                "18760000001",
                "POS",
                1,
                1000,
                0,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                FiscalEnvironment.TEST,
                true));

        mockMvc.perform(post("/api/v1/numbering-resolutions")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "documentType":"ELECTRONIC_POS",
                          "resolutionNumber":"18760000001",
                          "prefix":"POS",
                          "fromNumber":1,
                          "toNumber":1000,
                          "validFrom":"2026-01-01",
                          "validTo":"2026-12-31",
                          "environment":"TEST"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentType").value("ELECTRONIC_POS"))
                .andExpect(jsonPath("$.prefix").value("POS"));
    }

    @Test
    void issueElectronicPosReturnsCreatedDocument() throws Exception {
        when(issueElectronicPosUseCase.issue(any())).thenReturn(posResult(ElectronicDocumentStatus.NUMBER_ASSIGNED));

        mockMvc.perform(post("/api/v1/electronic-pos")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "buyerName":"Consumidor Final",
                          "buyerDocumentType":"CC",
                          "buyerDocumentNumber":"123456789",
                          "documentDate":"2026-05-15",
                          "environment":"TEST",
                          "lines":[
                            {
                              "quantity":2,
                              "unitPrice":15000,
                              "discountAmount":0,
                              "taxCode":"IVA_19",
                              "taxRate":19
                            }
                          ]
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(DOCUMENT_ID.toString()))
                .andExpect(jsonPath("$.prefix").value("POS"))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.status").value("NUMBER_ASSIGNED"));
    }

    @Test
    void findElectronicPosReturnsPersistedDocument() throws Exception {
        when(queryElectronicPosDocumentUseCase.findById(eq(COMPANY_ID), eq(DOCUMENT_ID)))
                .thenReturn(posResult(ElectronicDocumentStatus.VALIDATED));

        mockMvc.perform(get("/api/v1/electronic-pos/{documentId}", DOCUMENT_ID)
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(DOCUMENT_ID.toString()))
                .andExpect(jsonPath("$.status").value("VALIDATED"));
    }

    @Test
    void submitElectronicPosReturnsMockProviderOutcome() throws Exception {
        when(submitElectronicPosDocumentUseCase.submit(eq(COMPANY_ID), eq(DOCUMENT_ID), eq("idem-1")))
                .thenReturn(new SubmitElectronicPosDocumentResult(
                        DOCUMENT_ID,
                        "DUMMY-SUBMISSION",
                        ProviderSubmissionStatus.ACCEPTED,
                        ElectronicDocumentStatus.VALIDATED,
                        "DUMMY-CUDE",
                        "https://dummy-dian.local/documents/" + DOCUMENT_ID,
                        null,
                        null));

        mockMvc.perform(post("/api/v1/electronic-pos/{documentId}/submit", DOCUMENT_ID)
                .header("X-Company-Id", COMPANY_ID)
                .header("Idempotency-Key", "idem-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerStatus").value("ACCEPTED"))
                .andExpect(jsonPath("$.documentStatus").value("VALIDATED"))
                .andExpect(jsonPath("$.cufeCude").value("DUMMY-CUDE"));
    }

    private static ElectronicPosDocumentResult posResult(ElectronicDocumentStatus status) {
        return new ElectronicPosDocumentResult(
                DOCUMENT_ID,
                COMPANY_ID,
                null,
                "Consumidor Final",
                "CC",
                "123456789",
                "POS",
                1,
                "DUMMY-CUDE",
                new BigDecimal("30000.00"),
                new BigDecimal("5700.00"),
                new BigDecimal("35700.00"),
                status,
                Instant.parse("2026-05-15T18:00:00Z"));
    }
}
