package com.msvanegasg.facturaelectronica.expenses.interfaces.rest;

import com.msvanegasg.facturaelectronica.expenses.application.dto.ExpenseCommand;
import com.msvanegasg.facturaelectronica.expenses.domain.model.Expense;
import com.msvanegasg.facturaelectronica.expenses.interfaces.rest.dto.ExpenseRequest;
import com.msvanegasg.facturaelectronica.expenses.interfaces.rest.dto.ExpenseResponse;

public final class ExpenseRestMapper {

    private ExpenseRestMapper() {
    }

    public static ExpenseCommand toCommand(ExpenseRequest dto) {
        return new ExpenseCommand(
                dto.getFecha(),
                dto.getMonto(),
                dto.getDescripcion(),
                dto.getIdTipoGasto(),
                dto.getIdMetodoPago(),
                dto.getUrlEvidencia(),
                dto.getEstado());
    }

    public static ExpenseResponse toResponse(Expense expense) {
        return ExpenseResponse.builder()
                .fecha(expense.date())
                .monto(expense.amount())
                .descripcion(expense.description())
                .tipoGasto(ExpenseResponse.ExpenseTypeResponse.builder()
                        .id(expense.expenseType().id())
                        .nombre(expense.expenseType().name())
                        .descripcion(expense.expenseType().description())
                        .build())
                .metodoPago(ExpenseResponse.PaymentMethodResponse.builder()
                        .id(expense.paymentMethod().id())
                        .nombre(expense.paymentMethod().name())
                        .descripcion(expense.paymentMethod().description())
                        .build())
                .urlEvidencia(expense.evidenceUrl())
                .estado(expense.status())
                .build();
    }
}
