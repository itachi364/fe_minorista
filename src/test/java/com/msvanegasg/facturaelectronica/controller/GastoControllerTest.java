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

import com.msvanegasg.facturaelectronica.enums.Estado;
import com.msvanegasg.facturaelectronica.expenses.application.dto.ExpenseCommand;
import com.msvanegasg.facturaelectronica.expenses.application.port.in.ManageExpenseUseCase;
import com.msvanegasg.facturaelectronica.expenses.domain.model.Expense;
import com.msvanegasg.facturaelectronica.expenses.domain.model.ExpenseTypeSummary;
import com.msvanegasg.facturaelectronica.expenses.domain.model.PaymentMethodSummary;

@ExtendWith(MockitoExtension.class)
class GastoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ManageExpenseUseCase manageExpenseUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GastoController(manageExpenseUseCase))
                .build();
    }

    @Test
    void createExpenseKeepsLegacyEndpointAndResponseDto() throws Exception {
        when(manageExpenseUseCase.create(any(ExpenseCommand.class))).thenReturn(expense(true));

        mockMvc.perform(post("/api/gastos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(expenseJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monto").value(120000))
                .andExpect(jsonPath("$.tipoGasto.nombre").value("Administrativo"))
                .andExpect(jsonPath("$.metodoPago.nombre").value("Efectivo"));
    }

    @Test
    void updateExpenseKeepsLegacyEndpointAndResponseDto() throws Exception {
        when(manageExpenseUseCase.update(eq(1L), any(ExpenseCommand.class))).thenReturn(expense(true));

        mockMvc.perform(put("/api/gastos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(expenseJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Papeleria"));
    }

    @Test
    void disableExpenseReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/gastos/1"))
                .andExpect(status().isNoContent());

        verify(manageExpenseUseCase).disable(1L);
    }

    @Test
    void findByIdReturnsExpense() throws Exception {
        when(manageExpenseUseCase.findById(1L)).thenReturn(expense(true));

        mockMvc.perform(get("/api/gastos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PROCESADO"));
    }

    @Test
    void listActiveReturnsExpenses() throws Exception {
        when(manageExpenseUseCase.findActive()).thenReturn(List.of(expense(true)));

        mockMvc.perform(get("/api/gastos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void listByStatusReturnsExpenses() throws Exception {
        when(manageExpenseUseCase.findByStatus(Estado.PROCESADO)).thenReturn(List.of(expense(true)));

        mockMvc.perform(get("/api/gastos/estado/PROCESADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void listByExpenseTypeReturnsExpenses() throws Exception {
        when(manageExpenseUseCase.findByExpenseType(3L)).thenReturn(List.of(expense(true)));

        mockMvc.perform(get("/api/gastos/tipo/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipoGasto.id").value(3));
    }

    @Test
    void listByPaymentMethodReturnsExpenses() throws Exception {
        when(manageExpenseUseCase.findByPaymentMethod(4L)).thenReturn(List.of(expense(true)));

        mockMvc.perform(get("/api/gastos/metodo/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].metodoPago.id").value(4));
    }

    @Test
    void findByDescriptionReturnsExpense() throws Exception {
        when(manageExpenseUseCase.findByDescription("Papeleria")).thenReturn(expense(true));

        mockMvc.perform(get("/api/gastos/descripcion/Papeleria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Papeleria"));
    }

    private static String expenseJson() {
        return """
                {"fecha":"2026-05-12T10:00:00","monto":120000,"descripcion":"Papeleria","idTipoGasto":3,"idMetodoPago":4,"urlEvidencia":"https://evidencia.local/gasto.pdf","estado":"PROCESADO"}
                """;
    }

    private static Expense expense(boolean active) {
        return Expense.restore(
                1L,
                LocalDateTime.of(2026, 5, 12, 10, 0),
                BigDecimal.valueOf(120000),
                "Papeleria",
                new ExpenseTypeSummary(3L, "Administrativo", "Gastos administrativos"),
                new PaymentMethodSummary(4L, "Efectivo", "Pago en efectivo"),
                "https://evidencia.local/gasto.pdf",
                Estado.PROCESADO,
                active);
    }
}
