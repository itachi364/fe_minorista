package com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.in.FindProviderSubmissionUseCase;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.in.SubmitProviderDocumentUseCase;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderDocumentType;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderSubmissionStatus;
import com.msvanegasg.facturaelectronica.dianprovider.exception.DianProviderExceptionHandler;
import com.msvanegasg.facturaelectronica.dianprovider.observability.CorrelationId;
import com.msvanegasg.facturaelectronica.dianprovider.observability.CorrelationIdFilter;

@ExtendWith(MockitoExtension.class)
class DianProviderControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID DOCUMENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SUBMISSION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    private MockMvc mockMvc;

    @Mock
    private SubmitProviderDocumentUseCase submitUseCase;

    @Mock
    private FindProviderSubmissionUseCase findUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DianProviderController(submitUseCase, findUseCase))
                .setControllerAdvice(new DianProviderExceptionHandler())
                .addFilters(new CorrelationIdFilter())
                .build();
    }

    @Test
    void submitsElectronicPos() throws Exception {
        when(submitUseCase.submit(any())).thenReturn(result());

        mockMvc.perform(post("/api/v1/provider/electronic-pos")
                .header("Idempotency-Key", "confirm-1")
                .header(CorrelationId.HEADER_NAME, "corr-provider")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson()))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationId.HEADER_NAME, "corr-provider"))
                .andExpect(jsonPath("$.trackingId").value("mock-tracking"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.artifacts[0].type").value("XML"));
    }

    @Test
    void findsSubmissionByTrackingId() throws Exception {
        when(findUseCase.findByTrackingId(COMPANY_ID, "mock-tracking")).thenReturn(result());

        mockMvc.perform(get("/api/v1/provider/submissions/{trackingId}", "mock-tracking")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(DOCUMENT_ID.toString()));
    }

    @Test
    void validatesRequest() throws Exception {
        mockMvc.perform(post("/api/v1/provider/electronic-pos")
                .header("Idempotency-Key", "confirm-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private static ProviderSubmissionResult result() {
        return new ProviderSubmissionResult(SUBMISSION_ID, COMPANY_ID, DOCUMENT_ID, ProviderDocumentType.ELECTRONIC_POS,
                "mock-tracking", ProviderSubmissionStatus.ACCEPTED, "mock-cude", "mock-qr", null, null, NOW,
                "{\"status\":\"ACCEPTED\"}");
    }

    private static String requestJson() {
        return """
                {
                  "companyId": "11111111-1111-1111-1111-111111111111",
                  "documentId": "22222222-2222-2222-2222-222222222222",
                  "payload": {
                    "total": "35700.00"
                  }
                }
                """;
    }
}
