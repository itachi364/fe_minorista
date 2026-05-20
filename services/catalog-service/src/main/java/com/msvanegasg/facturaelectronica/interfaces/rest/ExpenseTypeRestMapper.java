package com.msvanegasg.facturaelectronica.catalog.interfaces.rest;

import com.msvanegasg.facturaelectronica.catalog.application.dto.ExpenseTypeCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.ExpenseType;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.ExpenseTypeRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.ExpenseTypeResponse;

public final class ExpenseTypeRestMapper {

    private ExpenseTypeRestMapper() {
    }

    public static ExpenseTypeCommand toCommand(ExpenseTypeRequest dto) {
        return new ExpenseTypeCommand(dto.getNombre(), dto.getDescripcion());
    }

    public static ExpenseTypeResponse toResponse(ExpenseType expenseType) {
        return ExpenseTypeResponse.builder()
                .idTipoGasto(expenseType.id())
                .nombre(expenseType.name())
                .descripcion(expenseType.description())
                .activo(expenseType.active())
                .build();
    }

    public static ExpenseTypeRequest toRequest(ExpenseType expenseType) {
        return ExpenseTypeRequest.builder()
                .nombre(expenseType.name())
                .descripcion(expenseType.description())
                .build();
    }
}
