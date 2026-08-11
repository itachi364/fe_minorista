package com.msvanegasg.facturaelectronica.inventory.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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

import com.msvanegasg.facturaelectronica.inventory.application.dto.ConfirmedServiceSupplyConsumptionResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ServiceSupplyReferenceResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.SuggestedSupplyConsumptionResult;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManageServiceSupplyReferenceUseCase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;
import com.msvanegasg.facturaelectronica.inventory.exception.InventoryExceptionHandler;
import com.msvanegasg.facturaelectronica.inventory.observability.CorrelationIdFilter;

@ExtendWith(MockitoExtension.class)
class ServiceSupplyReferenceControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SERVICE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SUPPLY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID REFERENCE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID SALE_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID USER_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID MOVEMENT_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
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

    @Test
    void suggestsSupplyConsumptions() throws Exception {
        when(useCase.suggestConsumptions(COMPANY_ID, SERVICE_ID)).thenReturn(List.of(suggestion()));

        mockMvc.perform(get("/api/v1/products/{serviceProductId}/supply-consumption-suggestions", SERVICE_ID)
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].supplyProductId").value(SUPPLY_ID.toString()))
                .andExpect(jsonPath("$[0].supplyName").value("Esmalte"))
                .andExpect(jsonPath("$[0].currentStock").value(10.0));
    }

    @Test
    void confirmsSupplyConsumption() throws Exception {
        when(useCase.confirmConsumption(any())).thenReturn(confirmedConsumption());

        mockMvc.perform(post("/api/v1/service-supply-consumptions")
                .header("X-Company-Id", COMPANY_ID)
                .header("X-User-Id", USER_ID)
                .header("Idempotency-Key", "service-consumption-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(consumptionJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceDocumentId").value(SALE_ID.toString()))
                .andExpect(jsonPath("$.movements[0].movementType").value("CONSUMPTION_OUT"))
                .andExpect(jsonPath("$.movements[0].resultingStock").value(9.75));
    }

    private static ServiceSupplyReferenceResult reference() {
        return new ServiceSupplyReferenceResult(REFERENCE_ID, COMPANY_ID, SERVICE_ID, SUPPLY_ID, "Esmalte sugerido",
                true, NOW);
    }

    private static SuggestedSupplyConsumptionResult suggestion() {
        return new SuggestedSupplyConsumptionResult(SERVICE_ID, SUPPLY_ID, "SUP-1", "Esmalte",
                new BigDecimal("10.00"), new BigDecimal("1500.00"), "Esmalte sugerido");
    }

    private static ConfirmedServiceSupplyConsumptionResult confirmedConsumption() {
        return new ConfirmedServiceSupplyConsumptionResult(SERVICE_ID, SALE_ID, List.of(new InventoryMovementResult(
                MOVEMENT_ID, COMPANY_ID, SUPPLY_ID, InventoryMovementType.CONSUMPTION_OUT, new BigDecimal("0.25"),
                new BigDecimal("1500.00"), new BigDecimal("10.00"), new BigDecimal("9.75"),
                InventorySourceDocumentType.MANUAL_SUPPLY_CONSUMPTION, SALE_ID,
                "service-consumption-1-service-supply-" + SUPPLY_ID, USER_ID,
                "Consumo real de insumos por manicura facturada", NOW)));
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

    private static String consumptionJson() {
        return """
                {
                  "serviceProductId": "22222222-2222-2222-2222-222222222222",
                  "sourceDocumentId": "55555555-5555-5555-5555-555555555555",
                  "reason": "Consumo real de insumos por manicura facturada",
                  "lines": [
                    {
                      "supplyProductId": "33333333-3333-3333-3333-333333333333",
                      "quantity": 0.25
                    }
                  ]
                }
                """;
    }
}
