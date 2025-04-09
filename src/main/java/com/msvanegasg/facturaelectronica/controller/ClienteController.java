package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.DTO.ClienteDTO;
import com.msvanegasg.facturaelectronica.DTO.response.ClienteResponseDTO;
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
	@Autowired
	private TipoDocumentoService tipoDocumentoService;

	@GetMapping
	public List<ClienteResponseDTO> findAll() {
		return clienteService.findAll().stream().map(ClienteMapper::toResponseDTO).collect(Collectors.toList());
	}

	@GetMapping("/active")
	public List<ClienteResponseDTO> findActivos() {
		return clienteService.findActivos().stream().map(ClienteMapper::toResponseDTO).collect(Collectors.toList());
	}

	@GetMapping("/inactive")
	public List<ClienteResponseDTO> findActivoFalse() {
		return clienteService.findActivosFalse().stream().map(ClienteMapper::toResponseDTO)
				.collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ClienteResponseDTO> findById(@PathVariable("id") Long id) {
		Cliente cliente = clienteService.findById(id);
		return ResponseEntity.ok(ClienteMapper.toResponseDTO(cliente));
	}

	@GetMapping("/documento/{numeroDocumento}/tipoDocumento/{tipoDocumento}")
	public ResponseEntity<ClienteResponseDTO> findByNumeroDocumento(
			@PathVariable("numeroDocumento") Long numeroDocumento, @PathVariable("tipoDocumento") Long tipoDocumento) {
		Cliente cliente = clienteService.findByNumeroDocumentoAndTipoDocumento(numeroDocumento, tipoDocumento);
		return ResponseEntity.ok(ClienteMapper.toResponseDTO(cliente));
	}

	@GetMapping("/nombre/{nombre}")
	public ResponseEntity<ClienteResponseDTO> findByNombre(@PathVariable("nombre") String nombre) {
		Cliente cliente = clienteService.findByNombre(nombre);
		return ResponseEntity.ok(ClienteMapper.toResponseDTO(cliente));
	}

	@PostMapping
	public ResponseEntity<ClienteDTO> save(@Valid @RequestBody ClienteDTO clienteDTO) {
		TipoDocumento tipoDocumento = tipoDocumentoService.findById(clienteDTO.getIdTipoDocumento());
		Cliente cliente = ClienteMapper.toEntity(clienteDTO, tipoDocumento);

		Cliente saved = clienteService.save(cliente);
		return ResponseEntity.ok(ClienteMapper.toDTO(saved));
	}

	@PutMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}")
	public ResponseEntity<ClienteDTO> update(@PathVariable("numeroDocumento") Long numeroDocumento,
			@PathVariable("tipoDocumento") Long tipoDocumento, @Valid @RequestBody ClienteDTO clienteDTO) {

		TipoDocumento tipoDoc = tipoDocumentoService.findById(tipoDocumento);

		Cliente clienteActualizado = ClienteMapper.toEntity(clienteDTO, tipoDoc);

		Cliente updated = clienteService.update(numeroDocumento, tipoDocumento, clienteActualizado);
		return ResponseEntity.ok(ClienteMapper.toDTO(updated));
	}

	@DeleteMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}")
	public ResponseEntity<Void> disable(@PathVariable("numeroDocumento") Long numeroDocumento,
			@PathVariable("tipoDocumento") Long tipoDocumento) {
		clienteService.disableByNumero(numeroDocumento, tipoDocumento);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{numeroDocumento}/tipo/{tipoDocumento}/activar")
	public ResponseEntity<Void> activarCliente(@PathVariable("numeroDocumento") Long numeroDocumento,
			@PathVariable("tipoDocumento") Long tipoDocumento) {
		clienteService.activarCliente(numeroDocumento, tipoDocumento);
		return ResponseEntity.noContent().build();
	}
}
