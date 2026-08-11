package com.msvanegasg.facturaelectronica.audit.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msvanegasg.facturaelectronica.audit.application.dto.AuditEventResult;
import com.msvanegasg.facturaelectronica.audit.application.port.in.QueryAuditEventsUseCase;
import com.msvanegasg.facturaelectronica.audit.application.port.in.RegisterAuditEventUseCase;
import com.msvanegasg.facturaelectronica.audit.domain.model.AuditResult;
import com.msvanegasg.facturaelectronica.audit.exception.AuditExceptionHandler;

class AuditEventControllerTest {

    private static final UUID EVENT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");

    private RegisterAuditEventUseCase registerAuditEventUseCase;
    private QueryAuditEventsUseCase queryAuditEventsUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        registerAuditEventUseCase = mock(RegisterAuditEventUseCase.class);
        queryAuditEventsUseCase = mock(QueryAuditEventsUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuditEventController(registerAuditEventUseCase,
                queryAuditEventsUseCase))
                .setControllerAdvice(new AuditExceptionHandler())
                .build();
    }

    @Test
    void registersAuditEvent() throws Exception {
        when(registerAuditEventUseCase.register(any())).thenReturn(event());

        mockMvc.perform(post("/api/v1/audit-events")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "userId": "%s",
                          "eventType": "ELECTRONIC_DOCUMENT",
                          "resourceType": "SALE",
                          "resourceId": "sale-1",
                          "action": "VALIDATED",
                          "result": "SUCCESS",
                          "detail": "{\\"status\\":\\"ACCEPTED\\"}"
                        }
                        """.formatted(USER_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(EVENT_ID.toString()))
                .andExpect(jsonPath("$.companyId").value(COMPANY_ID.toString()))
                .andExpect(jsonPath("$.result").value("SUCCESS"));
    }

    @Test
    void queriesAuditEvents() throws Exception {
        when(queryAuditEventsUseCase.find(any())).thenReturn(List.of(event()));

        mockMvc.perform(get("/api/v1/audit-events")
                .header("X-Company-Id", COMPANY_ID)
                .param("resourceType", "SALE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].resourceType").value("SALE"))
                .andExpect(jsonPath("$[0].resourceId").value("sale-1"));
    }

    @Test
    void listsResourceTypes() throws Exception {
        when(queryAuditEventsUseCase.resourceTypes(COMPANY_ID)).thenReturn(List.of("CATALOG", "SALE"));

        mockMvc.perform(get("/api/v1/audit-events/resource-types")
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("CATALOG"))
                .andExpect(jsonPath("$[1]").value("SALE"));
    }

    @Test
    void rejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/audit-events")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "eventType": "",
                          "resourceType": "SALE",
                          "action": "VALIDATED",
                          "result": "SUCCESS"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private static AuditEventResult event() {
        return new AuditEventResult(EVENT_ID, COMPANY_ID, USER_ID, "ELECTRONIC_DOCUMENT", "SALE", "sale-1",
                "VALIDATED", AuditResult.SUCCESS, "{\"status\":\"ACCEPTED\"}", NOW);
    }
}
