package com.msvanegasg.facturaelectronica.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.DTO.ClienteDTO;
import com.msvanegasg.facturaelectronica.DTO.response.ClienteResponseDTO;
import com.msvanegasg.facturaelectronica.service.ClienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        List<ClienteResponseDTO> clientes = clienteService.listarClientesActivos();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ClienteResponseDTO>> listarActivos() {
        return ResponseEntity.ok(clienteService.listarClientesActivos());
    }

    @GetMapping("/inactivos")
    public ResponseEntity<List<ClienteResponseDTO>> listarInactivos() {
        return ResponseEntity.ok(clienteService.listarClientesInactivos());
    }

    @GetMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}")
    public ResponseEntity<ClienteResponseDTO> obtenerPorDocumento(
            @PathVariable("numeroDocumento") Long numeroDocumento,
            @PathVariable("tipoDocumento") Long tipoDocumento) {
        return ResponseEntity.ok(clienteService
                .obtenerClientePorTipoYNumeroDocumento(tipoDocumento, numeroDocumento)
                .map(cliente -> clienteService.obtenerClientePorTipoYNumeroDocumento(tipoDocumento, numeroDocumento))
                .map(cliente -> clienteService.buscarPorNombre(cliente.getNombre()))
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"))); // Puedes usar tu excepción personalizada aquí
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<List<ClienteResponseDTO>> buscarPorNombre(@PathVariable("nombre") String nombre) {
        return ResponseEntity.ok(clienteService.buscarPorNombre(nombre));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crearCliente(@Valid @RequestBody ClienteDTO clienteDTO) {
        ClienteResponseDTO response = clienteService.crearCliente(clienteDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}")
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(
            @PathVariable("numeroDocumento") Long numeroDocumento,
            @PathVariable("tipoDocumento") Long tipoDocumento,
            @Valid @RequestBody ClienteDTO clienteDTO) {
        ClienteResponseDTO actualizado = clienteService.actualizarCliente(clienteDTO, numeroDocumento, tipoDocumento);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}")
    public ResponseEntity<Void> desactivarCliente(
            @PathVariable("numeroDocumento") Long numeroDocumento,
            @PathVariable("tipoDocumento") Long tipoDocumento) {
        clienteService.eliminarCliente(numeroDocumento, tipoDocumento);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}/activar")
    public ResponseEntity<Void> activarCliente(
            @PathVariable("numeroDocumento") Long numeroDocumento,
            @PathVariable("tipoDocumento") Long tipoDocumento) {
        clienteService.activarCliente(numeroDocumento, tipoDocumento);
        return ResponseEntity.noContent().build();
    }
}


