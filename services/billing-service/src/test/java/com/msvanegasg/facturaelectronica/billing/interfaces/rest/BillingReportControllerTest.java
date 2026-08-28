package com.msvanegasg.facturaelectronica.billing.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleLineResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ManageSaleUseCase;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.PaymentMethodCode;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleChannel;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleItemType;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleStatus;

@ExtendWith(MockitoExtension.class)
class BillingReportControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SALE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PRODUCT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID DOCUMENT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    private MockMvc mockMvc;

    @Mock
    private ManageSaleUseCase saleUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new BillingReportController(saleUseCase)).build();
    }

    @Test
    void reportsSalesByCompanyAndDateRange() throws Exception {
        when(saleUseCase.find(any())).thenReturn(List.of(sale()));

        mockMvc.perform(get("/api/v1/reports/sales")
                .header("X-Company-Id", COMPANY_ID)
                .param("status", "CONFIRMED")
                .param("from", "2026-05-01")
                .param("to", "2026-05-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(SALE_ID.toString()))
                .andExpect(jsonPath("$[0].total").value(35700));
    }

    @Test
    void reportsSalesWithProductAndSellerFilters() throws Exception {
        UUID sellerId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        when(saleUseCase.find(any())).thenReturn(List.of(sale()));

        mockMvc.perform(get("/api/v1/reports/sales")
                .header("X-Company-Id", COMPANY_ID)
                .param("status", "CONFIRMED")
                .param("from", "2026-05-01")
                .param("to", "2026-05-31")
                .param("sellerId", sellerId.toString())
                .param("productId", PRODUCT_ID.toString())
                .param("paymentMethodCode", "CASH")
                .param("documentStatus", "VALIDATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(SALE_ID.toString()));

        ArgumentCaptor<com.msvanegasg.facturaelectronica.billing.application.dto.SaleQuery> query =
                ArgumentCaptor.forClass(com.msvanegasg.facturaelectronica.billing.application.dto.SaleQuery.class);
        verify(saleUseCase).find(query.capture());
        assertThat(query.getValue().sellerId()).isEqualTo(sellerId);
        assertThat(query.getValue().productId()).isEqualTo(PRODUCT_ID);
        assertThat(query.getValue().paymentMethodCode()).isEqualTo(PaymentMethodCode.CASH);
        assertThat(query.getValue().documentStatus()).isEqualTo(ElectronicDocumentStatus.VALIDATED);
    }

    @Test
    void reportsElectronicDocumentsByCompanyAndFilters() throws Exception {
        when(saleUseCase.findElectronicDocuments(any())).thenReturn(List.of(document()));

        mockMvc.perform(get("/api/v1/reports/electronic-documents")
                .header("X-Company-Id", COMPANY_ID)
                .param("documentType", "ELECTRONIC_POS")
                .param("status", "VALIDATED")
                .param("prefix", "SETP")
                .param("number", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(DOCUMENT_ID.toString()))
                .andExpect(jsonPath("$[0].status").value("VALIDATED"));
    }

    private static SaleResult sale() {
        return new SaleResult(SALE_ID, COMPANY_ID, null, PaymentMethodCode.CASH, null, SaleChannel.POS, SaleStatus.CONFIRMED,
                new BigDecimal("30000.00"), BigDecimal.ZERO, new BigDecimal("5700.00"),
                new BigDecimal("35700.00"), "sale-1", null, NOW, NOW,
                List.of(new SaleLineResult(UUID.fromString("44444444-4444-4444-4444-444444444444"), PRODUCT_ID,
                        "SKU-1", "Producto", SaleItemType.PHYSICAL_GOOD, true, new BigDecimal("2.00"),
                        new BigDecimal("15000.00"), new BigDecimal("9000.00"), BigDecimal.ZERO, "IVA_19", new BigDecimal("19.00"),
                        new BigDecimal("30000.00"), new BigDecimal("5700.00"), new BigDecimal("35700.00"))),
                null);
    }

    private static ElectronicDocumentResult document() {
        return new ElectronicDocumentResult(DOCUMENT_ID, COMPANY_ID, SALE_ID, ElectronicDocumentType.ELECTRONIC_POS,
                ElectronicDocumentStatus.VALIDATED, ProviderStatus.ACCEPTED, "SETP", 1L, "mock-cude",
                "mock-qr", new BigDecimal("30000.00"), new BigDecimal("5700.00"), new BigDecimal("35700.00"),
                "tracking-1", null, null, NOW, NOW, NOW);
    }
}
