package com.msvanegasg.facturaelectronica.inventory.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementQuery;
import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ProductResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseResult;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManageProductUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManagePurchaseUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.RegisterInventoryMovementUseCase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryItemType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PaymentCondition;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseStatus;

@ExtendWith(MockitoExtension.class)
class InventoryReportControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID MOVEMENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    private MockMvc mockMvc;

    @Mock
    private ManageProductUseCase productUseCase;

    @Mock
    private RegisterInventoryMovementUseCase movementUseCase;

    @Mock
    private ManagePurchaseUseCase purchaseUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new InventoryReportController(productUseCase, movementUseCase, purchaseUseCase)).build();
    }

    @Test
    void reportsInventoryStockByCompany() throws Exception {
        when(productUseCase.findStock(COMPANY_ID, true)).thenReturn(List.of(product()));

        mockMvc.perform(get("/api/v1/reports/inventory-stock")
                .header("X-Company-Id", COMPANY_ID)
                .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(PRODUCT_ID.toString()))
                .andExpect(jsonPath("$[0].currentStock").value(10));
    }

    @Test
    void reportsKardexByProductAndDateRange() throws Exception {
        when(movementUseCase.kardex(any(InventoryMovementQuery.class))).thenReturn(List.of(movement()));

        mockMvc.perform(get("/api/v1/reports/kardex")
                .header("X-Company-Id", COMPANY_ID)
                .param("productId", PRODUCT_ID.toString())
                .param("from", "2026-05-01")
                .param("to", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].movementType").value("PURCHASE_IN"))
                .andExpect(jsonPath("$[0].resultingStock").value(10));
    }

    @Test
    void reportsPurchasesByCompanyAndDateRange() throws Exception {
        when(purchaseUseCase.find(any())).thenReturn(List.of(purchase()));

        mockMvc.perform(get("/api/v1/reports/purchases")
                .header("X-Company-Id", COMPANY_ID)
                .param("status", "CONFIRMED")
                .param("from", "2026-05-01")
                .param("to", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[0].total").value(119000));
    }

    private static ProductResult product() {
        return new ProductResult(PRODUCT_ID, COMPANY_ID, "SKU-1", "770123", "Cafe", "Bolsa 500g",
                InventoryItemType.PHYSICAL_GOOD, true, true, true, new BigDecimal("15000.00"),
                new BigDecimal("9000.00"), true, new BigDecimal("10.00"), NOW, NOW);
    }

    private static PurchaseResult purchase() {
        return new PurchaseResult(UUID.fromString("66666666-6666-6666-6666-666666666666"), COMPANY_ID, null,
                PurchaseStatus.CONFIRMED, new BigDecimal("100000.00"), new BigDecimal("19000.00"),
                new BigDecimal("119000.00"), PaymentCondition.CREDIT, java.time.LocalDate.of(2026, 6, 20), null,
                "purchase-1", NOW, NOW, List.of());
    }

    private static InventoryMovementResult movement() {
        return new InventoryMovementResult(MOVEMENT_ID, COMPANY_ID, PRODUCT_ID, InventoryMovementType.PURCHASE_IN,
                new BigDecimal("10.00"), new BigDecimal("9000.00"), BigDecimal.ZERO, new BigDecimal("10.00"),
                InventorySourceDocumentType.PURCHASE, UUID.fromString("44444444-4444-4444-4444-444444444444"),
                "purchase-1", null, null, NOW);
    }
}
