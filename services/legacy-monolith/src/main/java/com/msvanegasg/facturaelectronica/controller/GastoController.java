package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.enums.Estado;
import com.msvanegasg.facturaelectronica.expenses.application.port.in.ManageExpenseUseCase;
import com.msvanegasg.facturaelectronica.expenses.interfaces.rest.ExpenseRestMapper;
import com.msvanegasg.facturaelectronica.expenses.interfaces.rest.dto.ExpenseRequest;
import com.msvanegasg.facturaelectronica.expenses.interfaces.rest.dto.ExpenseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gastos")
@RequiredArgsConstructor
public class GastoController {

    private final ManageExpenseUseCase manageExpenseUseCase;

    @PostMapping
    public ResponseEntity<ExpenseResponse> crearGasto(@Valid @RequestBody ExpenseRequest gastoDTO) {
        return ResponseEntity.ok(ExpenseRestMapper.toResponse(
                manageExpenseUseCase.create(ExpenseRestMapper.toCommand(gastoDTO))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> actualizarGasto(@PathVariable("id") Long id,
            @Valid @RequestBody ExpenseRequest gastoDTO) {
        return ResponseEntity.ok(ExpenseRestMapper.toResponse(
                manageExpenseUseCase.update(id, ExpenseRestMapper.toCommand(gastoDTO))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarGasto(@PathVariable("id") Long id) {
        manageExpenseUseCase.disable(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> obtenerGasto(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ExpenseRestMapper.toResponse(manageExpenseUseCase.findById(id)));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> listarGastosActivos() {
        List<ExpenseResponse> gastos = manageExpenseUseCase.findActive().stream()
                .map(ExpenseRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(gastos);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ExpenseResponse>> listarPorEstado(@PathVariable("estado") Estado estado) {
        List<ExpenseResponse> gastos = manageExpenseUseCase.findByStatus(estado).stream()
                .map(ExpenseRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(gastos);
    }

    @GetMapping("/tipo/{idTipoGasto}")
    public ResponseEntity<List<ExpenseResponse>> listarPorTipo(@PathVariable("idTipoGasto") Long idTipoGasto) {
        List<ExpenseResponse> gastos = manageExpenseUseCase.findByExpenseType(idTipoGasto).stream()
                .map(ExpenseRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(gastos);
    }

    @GetMapping("/metodo/{idMetodoPago}")
    public ResponseEntity<List<ExpenseResponse>> listarPorMetodoPago(@PathVariable("idMetodoPago") Long idMetodoPago) {
        List<ExpenseResponse> gastos = manageExpenseUseCase.findByPaymentMethod(idMetodoPago).stream()
                .map(ExpenseRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(gastos);
    }

    @GetMapping("/descripcion/{descripcion}")
    public ResponseEntity<ExpenseResponse> findByDescripcion(@PathVariable("descripcion") String descripcion) {
        return ResponseEntity.ok(ExpenseRestMapper.toResponse(manageExpenseUseCase.findByDescription(descripcion)));
    }
}
