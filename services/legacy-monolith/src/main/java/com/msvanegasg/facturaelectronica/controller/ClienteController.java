package com.msvanegasg.facturaelectronica.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.thirdparty.application.port.in.ManageCustomerUseCase;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.CustomerRestMapper;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto.CustomerRequest;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto.CustomerResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ManageCustomerUseCase manageCustomerUseCase;

    public ClienteController(ManageCustomerUseCase manageCustomerUseCase) {
        this.manageCustomerUseCase = manageCustomerUseCase;
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> listarTodos() {
        List<CustomerResponse> clientes = manageCustomerUseCase.findActive().stream()
                .map(CustomerRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<CustomerResponse>> listarActivos() {
        return ResponseEntity.ok(manageCustomerUseCase.findActive().stream()
                .map(CustomerRestMapper::toResponse)
                .toList());
    }

    @GetMapping("/inactivos")
    public ResponseEntity<List<CustomerResponse>> listarInactivos() {
        return ResponseEntity.ok(manageCustomerUseCase.findInactive().stream()
                .map(CustomerRestMapper::toResponse)
                .toList());
    }

    @GetMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}")
    public ResponseEntity<CustomerResponse> obtenerPorDocumento(
            @PathVariable("numeroDocumento") Long numeroDocumento,
            @PathVariable("tipoDocumento") Long tipoDocumento) {
        return ResponseEntity.ok(
                CustomerRestMapper.toResponse(manageCustomerUseCase.findByDocument(tipoDocumento, numeroDocumento)));
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<List<CustomerResponse>> buscarPorNombre(@PathVariable("nombre") String nombre) {
        return ResponseEntity.ok(manageCustomerUseCase.findByName(nombre).stream()
                .map(CustomerRestMapper::toResponse)
                .toList());
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> crearCliente(@Valid @RequestBody CustomerRequest clienteDTO) {
        return ResponseEntity.ok(CustomerRestMapper.toResponse(
                manageCustomerUseCase.create(CustomerRestMapper.toCommand(clienteDTO))));
    }

    @PutMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}")
    public ResponseEntity<CustomerResponse> actualizarCliente(
            @PathVariable("numeroDocumento") Long numeroDocumento,
            @PathVariable("tipoDocumento") Long tipoDocumento,
            @Valid @RequestBody CustomerRequest clienteDTO) {
        return ResponseEntity.ok(CustomerRestMapper.toResponse(
                manageCustomerUseCase.update(tipoDocumento, numeroDocumento, CustomerRestMapper.toCommand(clienteDTO))));
    }

    @DeleteMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}")
    public ResponseEntity<Void> desactivarCliente(
            @PathVariable("numeroDocumento") Long numeroDocumento,
            @PathVariable("tipoDocumento") Long tipoDocumento) {
        manageCustomerUseCase.disable(tipoDocumento, numeroDocumento);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}/activar")
    public ResponseEntity<Void> activarCliente(
            @PathVariable("numeroDocumento") Long numeroDocumento,
            @PathVariable("tipoDocumento") Long tipoDocumento) {
        manageCustomerUseCase.enable(tipoDocumento, numeroDocumento);
        return ResponseEntity.noContent().build();
    }
}
