package com.msvanegasg.facturaelectronica.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseCommand;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManagePurchaseUseCase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseLine;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseStatus;

@ExtendWith(MockitoExtension.class)
class CompraControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ManagePurchaseUseCase managePurchaseUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CompraController(managePurchaseUseCase))
                .build();
    }

    @Test
    void createPurchaseKeepsLegacyEndpointAndResponseDto() throws Exception {
        when(managePurchaseUseCase.create(any(PurchaseCommand.class))).thenReturn(purchase());

        mockMvc.perform(post("/api/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(purchaseJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(300))
                .andExpect(jsonPath("$.detalles", hasSize(1)))
                .andExpect(jsonPath("$.detalles[0].codigoBarras").value(123456789));
    }

    @Test
    void findByIdReturnsPurchase() throws Exception {
        when(managePurchaseUseCase.findById(1L)).thenReturn(purchase());

        mockMvc.perform(get("/api/compras/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(357))
                .andExpect(jsonPath("$.detalles[0].cantidad").value(3));
    }

    @Test
    void findDetailsReturnsPurchaseDetails() throws Exception {
        when(managePurchaseUseCase.findDetailsByPurchaseId(1L)).thenReturn(purchase().lines());

        mockMvc.perform(get("/api/compras/1/detalles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].totalLinea").value(357));
    }

    @Test
    void listActiveReturnsPurchases() throws Exception {
        when(managePurchaseUseCase.findActive()).thenReturn(List.of(purchase()));

        mockMvc.perform(get("/api/compras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ivaTotal").value(57));
    }

    private static String purchaseJson() {
        return """
                {"numeroDocumento":900123456,"tipoDocumentoId":13,"subtotal":300,"ivaTotal":57,"total":357,"urlEvidencia":"https://evidencia.local/factura.pdf","detalles":[{"codigoBarras":123456789,"cantidad":3,"precioUnitario":100,"subtotal":300,"iva":57,"totalLinea":357}]}
                """;
    }

    private static Purchase purchase() {
        return Purchase.restore(
                1L,
                99L,
                LocalDateTime.of(2026, 5, 12, 10, 0),
                BigDecimal.valueOf(300),
                BigDecimal.valueOf(57),
                BigDecimal.valueOf(357),
                "https://evidencia.local/factura.pdf",
                PurchaseStatus.PROCESSED,
                true,
                List.of(PurchaseLine.restore(5L, 123456789L, 3, BigDecimal.valueOf(100), BigDecimal.valueOf(300),
                        BigDecimal.valueOf(57), BigDecimal.valueOf(357), true)));
    }
}
