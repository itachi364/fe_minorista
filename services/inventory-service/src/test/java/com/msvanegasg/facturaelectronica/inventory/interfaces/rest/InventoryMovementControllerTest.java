package com.msvanegasg.facturaelectronica.inventory.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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

import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementResult;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.RegisterInventoryMovementUseCase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;
import com.msvanegasg.facturaelectronica.inventory.exception.InventoryExceptionHandler;
import com.msvanegasg.facturaelectronica.inventory.observability.CorrelationIdFilter;

@ExtendWith(MockitoExtension.class)
class InventoryMovementControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID MOVEMENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID SOURCE_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    private MockMvc mockMvc;

    @Mock
    private RegisterInventoryMovementUseCase movementUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new InventoryMovementController(movementUseCase))
                .setControllerAdvice(new InventoryExceptionHandler())
                .addFilters(new CorrelationIdFilter())
                .build();
    }

    @Test
    void registersMovement() throws Exception {
        when(movementUseCase.register(any())).thenReturn(movement());

        mockMvc.perform(post("/api/v1/inventory-movements")
                .header("X-Company-Id", COMPANY_ID)
                .header("Idempotency-Key", "adjustment-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "productId": "22222222-2222-2222-2222-222222222222",
                          "movementType": "ADJUSTMENT_IN",
                          "quantity": 4.00,
                          "unitCost": 9000.00,
                          "sourceDocumentType": "ADJUSTMENT",
                          "sourceDocumentId": "44444444-4444-4444-4444-444444444444"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(MOVEMENT_ID.toString()))
                .andExpect(jsonPath("$.resultingStock").value(4));
    }

    @Test
    void requiresIdempotencyKey() throws Exception {
        mockMvc.perform(post("/api/v1/inventory-movements")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private static InventoryMovementResult movement() {
        return new InventoryMovementResult(MOVEMENT_ID, COMPANY_ID, PRODUCT_ID, InventoryMovementType.ADJUSTMENT_IN,
                new BigDecimal("4.00"), new BigDecimal("9000.00"), BigDecimal.ZERO, new BigDecimal("4.00"),
                InventorySourceDocumentType.ADJUSTMENT, SOURCE_ID, "adjustment-1", null, NOW);
    }
}
