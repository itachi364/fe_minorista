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

import com.msvanegasg.facturaelectronica.thirdparty.application.dto.CustomerCommand;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.CustomerResult;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.in.ManageCustomerUseCase;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ManageCustomerUseCase manageCustomerUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ClienteController(manageCustomerUseCase))
                .build();
    }

    @Test
    void listDefaultReturnsActiveCustomers() throws Exception {
        when(manageCustomerUseCase.findActive()).thenReturn(List.of(customer(true)));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idCliente").value(7))
                .andExpect(jsonPath("$[0].descripcionTipoDocumento").value("Cedula"));
    }

    @Test
    void listInactiveReturnsCustomers() throws Exception {
        when(manageCustomerUseCase.findInactive()).thenReturn(List.of(customer(false)));

        mockMvc.perform(get("/api/clientes/inactivos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].activo").value(false));
    }

    @Test
    void findByDocumentReturnsCustomer() throws Exception {
        when(manageCustomerUseCase.findByDocument(13L, 123456789L)).thenReturn(customer(true));

        mockMvc.perform(get("/api/clientes/documento/123456789/tipo/13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroDocumento").value(123456789))
                .andExpect(jsonPath("$.codigoTipoDocumento").value("13"));
    }

    @Test
    void findByNameReturnsCustomers() throws Exception {
        when(manageCustomerUseCase.findByName("Cliente")).thenReturn(List.of(customer(true)));

        mockMvc.perform(get("/api/clientes/nombre/Cliente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createCustomerKeepsLegacyEndpointAndResponseDto() throws Exception {
        when(manageCustomerUseCase.create(any(CustomerCommand.class))).thenReturn(customer(true));

        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(customerJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(7))
                .andExpect(jsonPath("$.tipoCliente").value("NATURAL"));
    }

    @Test
    void updateCustomerKeepsLegacyEndpointAndResponseDto() throws Exception {
        when(manageCustomerUseCase.update(eq(13L), eq(123456789L), any(CustomerCommand.class)))
                .thenReturn(customer(true));

        mockMvc.perform(put("/api/clientes/documento/123456789/tipo/13")
                .contentType(MediaType.APPLICATION_JSON)
                .content(customerJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cliente Prueba"));
    }

    @Test
    void disableCustomerReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/clientes/documento/123456789/tipo/13"))
                .andExpect(status().isNoContent());

        verify(manageCustomerUseCase).disable(13L, 123456789L);
    }

    @Test
    void enableCustomerReturnsNoContent() throws Exception {
        mockMvc.perform(put("/api/clientes/documento/123456789/tipo/13/activar"))
                .andExpect(status().isNoContent());

        verify(manageCustomerUseCase).enable(13L, 123456789L);
    }

    private static String customerJson() {
        return """
                {"nombre":"Cliente Prueba","idTipoDocumento":13,"numeroDocumento":123456789,"direccion":"Calle 1","telefono":"3001234567","correoElectronico":"cliente@example.com"}
                """;
    }

    private static CustomerResult customer(boolean active) {
        return new CustomerResult(
                7L,
                "Cliente Prueba",
                13L,
                "13",
                "Cedula",
                123456789L,
                null,
                "Calle 1",
                "3001234567",
                "cliente@example.com",
                "NATURAL",
                active);
    }
}
