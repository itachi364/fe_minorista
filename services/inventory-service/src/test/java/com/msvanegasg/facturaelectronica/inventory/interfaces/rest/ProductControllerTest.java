package com.msvanegasg.facturaelectronica.inventory.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementQuery;
import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ProductResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.StockAvailabilityResult;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManageProductUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.RegisterInventoryMovementUseCase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryItemType;
import com.msvanegasg.facturaelectronica.inventory.exception.InventoryExceptionHandler;
import com.msvanegasg.facturaelectronica.inventory.observability.CorrelationId;
import com.msvanegasg.facturaelectronica.inventory.observability.CorrelationIdFilter;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID MOVEMENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    private MockMvc mockMvc;

    @Mock
    private ManageProductUseCase productUseCase;

    @Mock
    private RegisterInventoryMovementUseCase movementUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProductController(productUseCase, movementUseCase))
                .setControllerAdvice(new InventoryExceptionHandler())
                .addFilters(new CorrelationIdFilter())
                .build();
    }

    @Test
    void createsProduct() throws Exception {
        when(productUseCase.create(any())).thenReturn(product());

        mockMvc.perform(post("/api/v1/products")
                .header("X-Company-Id", COMPANY_ID)
                .header("Idempotency-Key", "product-1")
                .header(CorrelationId.HEADER_NAME, "corr-inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string(CorrelationId.HEADER_NAME, "corr-inventory"))
                .andExpect(jsonPath("$.id").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$.itemType").value("PHYSICAL_GOOD"))
                .andExpect(jsonPath("$.stockTracked").value(true))
                .andExpect(jsonPath("$.currentStock").value(10));
    }

    @Test
    void checksAvailability() throws Exception {
        when(productUseCase.checkAvailability(eq(COMPANY_ID), eq(PRODUCT_ID), any()))
                .thenReturn(new StockAvailabilityResult(COMPANY_ID, PRODUCT_ID, new BigDecimal("2.00"),
                        new BigDecimal("10.00"), true));

        mockMvc.perform(get("/api/v1/products/{productId}/availability", PRODUCT_ID)
                .header("X-Company-Id", COMPANY_ID)
                .param("quantity", "2.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void listsProductsByActiveState() throws Exception {
        when(productUseCase.findStock(COMPANY_ID, true)).thenReturn(List.of(product()));

        mockMvc.perform(get("/api/v1/products")
                .header("X-Company-Id", COMPANY_ID)
                .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$[0].name").value("Cafe"))
                .andExpect(jsonPath("$[0].currentStock").value(10));
    }

    @Test
    void returnsKardex() throws Exception {
        when(movementUseCase.kardex(any(InventoryMovementQuery.class))).thenReturn(List.of(movement()));

        mockMvc.perform(get("/api/v1/products/{productId}/kardex", PRODUCT_ID)
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].movementType").value("PURCHASE_IN"))
                .andExpect(jsonPath("$[0].resultingStock").value(10));
    }

    @Test
    void validatesRequiredCompanyHeader() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private static ProductResult product() {
        return new ProductResult(PRODUCT_ID, COMPANY_ID, "SKU-1", "770123", "Cafe", "Bolsa 500g",
                InventoryItemType.PHYSICAL_GOOD, true, true, true, new BigDecimal("15000.00"),
                new BigDecimal("9000.00"), true, new BigDecimal("10.00"), NOW, NOW);
    }

    private static InventoryMovementResult movement() {
        return new InventoryMovementResult(MOVEMENT_ID, COMPANY_ID, PRODUCT_ID, InventoryMovementType.PURCHASE_IN,
                new BigDecimal("10.00"), new BigDecimal("9000.00"), BigDecimal.ZERO, new BigDecimal("10.00"),
                InventorySourceDocumentType.PURCHASE, UUID.fromString("44444444-4444-4444-4444-444444444444"),
                "purchase-1", null, null, NOW);
    }

    private static String productJson() {
        return """
                {
                  "sku": "SKU-1",
                  "barcode": "770123",
                  "name": "Cafe",
                  "description": "Bolsa 500g",
                  "salePrice": 15000.00,
                  "cost": 9000.00,
                  "initialStock": 10.00
                }
                """;
    }
}
