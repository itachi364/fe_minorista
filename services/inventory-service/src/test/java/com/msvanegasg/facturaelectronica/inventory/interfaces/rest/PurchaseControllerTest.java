package com.msvanegasg.facturaelectronica.inventory.interfaces.rest;

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

import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseQuery;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseLineResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseResult;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManagePurchaseUseCase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PaymentCondition;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseStatus;
import com.msvanegasg.facturaelectronica.inventory.exception.InventoryExceptionHandler;
import com.msvanegasg.facturaelectronica.inventory.observability.CorrelationIdFilter;

@ExtendWith(MockitoExtension.class)
class PurchaseControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PURCHASE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    private MockMvc mockMvc;

    @Mock
    private ManagePurchaseUseCase purchaseUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PurchaseController(purchaseUseCase))
                .setControllerAdvice(new InventoryExceptionHandler())
                .addFilters(new CorrelationIdFilter())
                .build();
    }

    @Test
    void createsPurchase() throws Exception {
        when(purchaseUseCase.create(any())).thenReturn(purchase(PurchaseStatus.PENDING));

        mockMvc.perform(post("/api/v1/purchases")
                .header("X-Company-Id", COMPANY_ID)
                .header("Idempotency-Key", "purchase-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(purchaseJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(PURCHASE_ID.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.paymentCondition").value("CREDIT"))
                .andExpect(jsonPath("$.lines[0].description").value("Factura proveedor cafe"));
    }

    @Test
    void confirmsPurchase() throws Exception {
        when(purchaseUseCase.confirm(eq(COMPANY_ID), eq(PURCHASE_ID), any())).thenReturn(purchase(PurchaseStatus.CONFIRMED));

        mockMvc.perform(post("/api/v1/purchases/{purchaseId}/confirm", PURCHASE_ID)
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void listsPurchasesWithFilters() throws Exception {
        when(purchaseUseCase.find(any(PurchaseQuery.class))).thenReturn(List.of(purchase(PurchaseStatus.CONFIRMED)));

        mockMvc.perform(get("/api/v1/purchases")
                .header("X-Company-Id", COMPANY_ID)
                .param("status", "CONFIRMED")
                .param("supplierId", "66666666-6666-6666-6666-666666666666")
                .param("from", "2026-06-01")
                .param("to", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(PURCHASE_ID.toString()))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[0].total").value(107100.00));
    }

    private static PurchaseResult purchase(PurchaseStatus status) {
        return new PurchaseResult(PURCHASE_ID, COMPANY_ID, null, status, new BigDecimal("90000.00"),
                new BigDecimal("17100.00"), new BigDecimal("107100.00"), PaymentCondition.CREDIT,
                LocalDate.of(2026, 6, 20), null, "purchase-1", NOW, status == PurchaseStatus.CONFIRMED ? NOW : null,
                List.of(new PurchaseLineResult(UUID.fromString("44444444-4444-4444-4444-444444444444"), null,
                        "Factura proveedor cafe",
                        new BigDecimal("10.00"), new BigDecimal("9000.00"), new BigDecimal("90000.00"),
                        new BigDecimal("17100.00"), new BigDecimal("107100.00"))));
    }

    private static String purchaseJson() {
        return """
                {
                  "subtotal": 90000.00,
                  "taxTotal": 17100.00,
                  "total": 107100.00,
                  "paymentCondition": "CREDIT",
                  "dueDate": "2026-06-20",
                  "lines": [
                    {
                      "description": "Factura proveedor cafe",
                      "quantity": 10.00,
                      "unitCost": 9000.00,
                      "subtotal": 90000.00,
                      "tax": 17100.00,
                      "total": 107100.00
                    }
                  ]
                }
                """;
    }
}
