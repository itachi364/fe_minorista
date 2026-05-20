package com.msvanegasg.facturaelectronica.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msvanegasg.facturaelectronica.catalog.application.dto.PaymentMethodCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManagePaymentMethodUseCase;
import com.msvanegasg.facturaelectronica.catalog.domain.model.PaymentMethod;

@ExtendWith(MockitoExtension.class)
class MetodoPagoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ManagePaymentMethodUseCase managePaymentMethodUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new MetodoPagoController(managePaymentMethodUseCase))
                .build();
    }

    @Test
    void findAllReturnsLegacyPaymentMethodShape() throws Exception {
        when(managePaymentMethodUseCase.findAll())
                .thenReturn(List.of(PaymentMethod.restore(1L, "Efectivo", "Pago en caja", true)));

        mockMvc.perform(get("/api/metodopago"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idMetodoPago").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Efectivo"))
                .andExpect(jsonPath("$[0].descripcion").value("Pago en caja"))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void getByIdReturnsLegacyDtoShape() throws Exception {
        when(managePaymentMethodUseCase.findById(1L))
                .thenReturn(PaymentMethod.restore(1L, "Efectivo", "Pago en caja", true));

        mockMvc.perform(get("/api/metodopago/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Efectivo"))
                .andExpect(jsonPath("$.descripcion").value("Pago en caja"));
    }

    @Test
    void createPaymentMethodKeepsLegacyEndpointAndDtoResponse() throws Exception {
        when(managePaymentMethodUseCase.create(any(PaymentMethodCommand.class)))
                .thenReturn(PaymentMethod.restore(1L, "Efectivo", "Pago en caja", true));

        mockMvc.perform(post("/api/metodopago")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Efectivo\",\"descripcion\":\"Pago en caja\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Efectivo"))
                .andExpect(jsonPath("$.descripcion").value("Pago en caja"));
    }

    @Test
    void updatePaymentMethodKeepsLegacyEndpointAndDtoResponse() throws Exception {
        when(managePaymentMethodUseCase.update(eq(1L), any(PaymentMethodCommand.class)))
                .thenReturn(PaymentMethod.restore(1L, "Tarjeta", "Pago con tarjeta", true));

        mockMvc.perform(put("/api/metodopago/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Tarjeta\",\"descripcion\":\"Pago con tarjeta\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Tarjeta"))
                .andExpect(jsonPath("$.descripcion").value("Pago con tarjeta"));
    }

    @Test
    void disablePaymentMethodReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/metodopago/1"))
                .andExpect(status().isNoContent());

        verify(managePaymentMethodUseCase).disable(1L);
    }

    @Test
    void enablePaymentMethodReturnsNoContent() throws Exception {
        mockMvc.perform(put("/api/metodopago/1/activar"))
                .andExpect(status().isNoContent());

        verify(managePaymentMethodUseCase).enable(1L);
    }
}
