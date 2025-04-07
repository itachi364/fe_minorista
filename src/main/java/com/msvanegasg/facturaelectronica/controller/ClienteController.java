package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.DTO.ClienteDTO;
import com.msvanegasg.facturaelectronica.mapper.ClienteMapper;
import com.msvanegasg.facturaelectronica.models.Cliente;
import com.msvanegasg.facturaelectronica.models.TipoDocumento;
import com.msvanegasg.facturaelectronica.service.ClienteService;
import com.msvanegasg.facturaelectronica.service.TipoDocumentoService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

	@Autowired
    private ClienteService clienteService;
	private TipoDocumentoService tipoDocumentoService;
	
    @GetMapping
    public ResponseEntity<List<Cliente>> findAll() {
    	List<Cliente> all = clienteService.findAll();
        return ResponseEntity.ok(all);
    }


    @GetMapping("/activos")
    public ResponseEntity<List<Cliente>> findActivos() {
        List<Cliente> activos = clienteService.findActivos();
        return ResponseEntity.ok(activos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> findById(@PathVariable("id") Long id) {
        Cliente cliente = clienteService.findById(id);
        return ResponseEntity.ok(cliente);
    }

    @GetMapping("/documento/{numeroDocumento}")
    public ResponseEntity<Cliente> findByNumeroDocumento(@PathVariable("numeroDocumento") Long numeroDocumento) {
        Cliente cliente = clienteService.findByNumeroDocumento(numeroDocumento);
        return ResponseEntity.ok(cliente);
    }


    @GetMapping("/buscar/nombre")
    public ResponseEntity<List<ClienteDTO>> findByNombre(@RequestParam String nombre) {
        List<ClienteDTO> clientes = clienteService.findByNombre(nombre).stream()
                .map(ClienteMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    @PostMapping
    public ResponseEntity<ClienteDTO> save(@Valid @RequestBody ClienteDTO clienteDTO) {
        TipoDocumento tipoDocumento = tipoDocumentoService.findById(clienteDTO.getIdTipoDocumento());
        Cliente cliente = ClienteMapper.toEntity(clienteDTO, tipoDocumento);
        Cliente saved = clienteService.save(cliente);
        return ResponseEntity.ok(ClienteMapper.toDTO(saved));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> update(@PathVariable("id") Long id, @Valid @RequestBody ClienteDTO clienteDTO) {
        TipoDocumento tipoDocumento = tipoDocumentoService.findById(clienteDTO.getIdTipoDocumento());
        Cliente clienteActualizado = ClienteMapper.toEntity(clienteDTO, tipoDocumento);
        Cliente actualizado = clienteService.update(id, clienteActualizado);
        return ResponseEntity.ok(ClienteMapper.toDTO(actualizado));
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disable(@PathVariable("id") Long id) {
        clienteService.disableById(id);
        return ResponseEntity.noContent().build();
    }
}
