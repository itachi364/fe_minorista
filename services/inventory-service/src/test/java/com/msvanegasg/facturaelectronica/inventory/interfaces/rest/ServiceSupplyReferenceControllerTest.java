package com.msvanegasg.facturaelectronica.inventory.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msvanegasg.facturaelectronica.inventory.application.dto.ServiceSupplyReferenceResult;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManageServiceSupplyReferenceUseCase;
import com.msvanegasg.facturaelectronica.inventory.exception.InventoryExceptionHandler;
import com.msvanegasg.facturaelectronica.inventory.observability.CorrelationIdFilter;

@ExtendWith(MockitoExtension.class)
class ServiceSupplyReferenceControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SERVICE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SUPPLY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID REFERENCE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final Instant NOW = Instant.parse("2026-05-20T10:00:00Z");

    private MockMvc mockMvc;

    @Mock
    private ManageServiceSupplyReferenceUseCase useCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ServiceSupplyReferenceController(useCase))
                .setControllerAdvice(new InventoryExceptionHandler())
                .addFilters(new CorrelationIdFilter())
                .build();
    }

    @Test
    void createsReference() throws Exception {
        when(useCase.create(any())).thenReturn(reference());

        mockMvc.perform(post("/api/v1/service-supply-references")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(referenceJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serviceProductId").value(SERVICE_ID.toString()))
                .andExpect(jsonPath("$.supplyProductId").value(SUPPLY_ID.toString()));
    }

    @Test
    void listsReferencesByService() throws Exception {
        when(useCase.findByService(COMPANY_ID, SERVICE_ID)).thenReturn(List.of(reference()));

        mockMvc.perform(get("/api/v1/products/{serviceProductId}/supply-references", SERVICE_ID)
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(REFERENCE_ID.toString()));
    }

    private static ServiceSupplyReferenceResult reference() {
        return new ServiceSupplyReferenceResult(REFERENCE_ID, COMPANY_ID, SERVICE_ID, SUPPLY_ID, "Esmalte sugerido",
                true, NOW);
    }

    private static String referenceJson() {
        return """
                {
                  "serviceProductId": "22222222-2222-2222-2222-222222222222",
                  "supplyProductId": "33333333-3333-3333-3333-333333333333",
                  "notes": "Esmalte sugerido"
                }
                """;
    }
}
