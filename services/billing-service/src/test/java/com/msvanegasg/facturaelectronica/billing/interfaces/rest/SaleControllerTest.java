package com.msvanegasg.facturaelectronica.billing.interfaces.rest;

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

import com.msvanegasg.facturaelectronica.billing.application.dto.SaleLineResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ManageSaleUseCase;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleItemType;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleStatus;
import com.msvanegasg.facturaelectronica.billing.exception.BillingExceptionHandler;
import com.msvanegasg.facturaelectronica.billing.observability.CorrelationId;
import com.msvanegasg.facturaelectronica.billing.observability.CorrelationIdFilter;

@ExtendWith(MockitoExtension.class)
class SaleControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SALE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PRODUCT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    private MockMvc mockMvc;

    @Mock
    private ManageSaleUseCase saleUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SaleController(saleUseCase))
                .setControllerAdvice(new BillingExceptionHandler())
                .addFilters(new CorrelationIdFilter())
                .build();
    }

    @Test
    void createsSale() throws Exception {
        when(saleUseCase.create(any())).thenReturn(result(SaleStatus.DRAFT));

        mockMvc.perform(post("/api/v1/sales")
                .header("X-Company-Id", COMPANY_ID)
                .header("Idempotency-Key", "sale-1")
                .header(CorrelationId.HEADER_NAME, "corr-billing")
                .contentType(MediaType.APPLICATION_JSON)
                .content(saleJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string(CorrelationId.HEADER_NAME, "corr-billing"))
                .andExpect(jsonPath("$.id").value(SALE_ID.toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.lines[0].itemType").value("PHYSICAL_GOOD"))
                .andExpect(jsonPath("$.lines[0].stockTracked").value(true));
    }

    @Test
    void confirmsSale() throws Exception {
        when(saleUseCase.confirm(eq(COMPANY_ID), eq(SALE_ID), eq("confirm-1"))).thenReturn(result(SaleStatus.CONFIRMED));

        mockMvc.perform(post("/api/v1/sales/{saleId}/confirm", SALE_ID)
                .header("X-Company-Id", COMPANY_ID)
                .header("Idempotency-Key", "confirm-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void findsSale() throws Exception {
        when(saleUseCase.findById(COMPANY_ID, SALE_ID)).thenReturn(result(SaleStatus.DRAFT));

        mockMvc.perform(get("/api/v1/sales/{saleId}", SALE_ID)
                .header("X-Company-Id", COMPANY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(35700));
    }

    @Test
    void validatesRequiredHeaders() throws Exception {
        mockMvc.perform(post("/api/v1/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    private static SaleResult result(SaleStatus status) {
        return new SaleResult(SALE_ID, COMPANY_ID, null, null, SaleChannel.POS, status, new BigDecimal("30000.00"),
                BigDecimal.ZERO, new BigDecimal("5700.00"), new BigDecimal("35700.00"), "sale-1", null, NOW,
                status == SaleStatus.CONFIRMED ? NOW : null,
                List.of(new SaleLineResult(UUID.fromString("44444444-4444-4444-4444-444444444444"), PRODUCT_ID,
                        "SKU-1", "Producto", SaleItemType.PHYSICAL_GOOD, true,
                        new BigDecimal("2.00"), new BigDecimal("15000.00"), new BigDecimal("9000.00"), BigDecimal.ZERO, "IVA_19",
                        new BigDecimal("19.00"), new BigDecimal("30000.00"), new BigDecimal("5700.00"),
                        new BigDecimal("35700.00"))),
                null);
    }

    private static String saleJson() {
        return """
                {
                  "saleChannel": "POS",
                  "items": [
                    {
                      "productId": "33333333-3333-3333-3333-333333333333",
                      "quantity": 2.00,
                      "unitPrice": 15000.00,
                      "discountAmount": 0,
                      "taxCode": "IVA_19",
                      "taxRate": 19.00
                    }
                  ]
                }
                """;
    }
}
