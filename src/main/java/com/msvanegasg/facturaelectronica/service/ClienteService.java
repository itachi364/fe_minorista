package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.exception.cliente.ClienteInactivoException;
import com.msvanegasg.facturaelectronica.exception.cliente.ClienteNotFoundException;
import com.msvanegasg.facturaelectronica.exception.cliente.ClienteIdNotFoundException;
import com.msvanegasg.facturaelectronica.models.Cliente;
import com.msvanegasg.facturaelectronica.models.TipoDocumento;
import com.msvanegasg.facturaelectronica.repository.ClienteRepository;
import com.msvanegasg.facturaelectronica.repository.TipoDocumentoRepository;
import com.msvanegasg.facturaelectronica.validator.EntidadValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private TipoDocumentoRepository tipoDocumentoRepository;

	@Autowired
	private EntidadValidator entidadValidator;

	public List<Cliente> findAll() {
		return clienteRepository.findAll();
	}

	public List<Cliente> findActivos() {
		return clienteRepository.findByActivoTrue();
	}

	public List<Cliente> findActivosFalse() {
		return clienteRepository.findByActivoFalse();
	}

	public Cliente findById(Long id) {
		return clienteRepository.findById(id).orElseThrow(() -> new ClienteIdNotFoundException(id));
	}

	public Cliente findByNumeroDocumentoAndTipoDocumento(Long numeroDocumento, Long tipoDocumentoCodigo) {
		TipoDocumento tipoDocumento = entidadValidator.obtenerTipoDocumento(tipoDocumentoCodigo);

		return clienteRepository.findByNumeroDocumentoAndTipoDocumento(numeroDocumento, tipoDocumento)
				.orElseThrow(() -> new ClienteNotFoundException(numeroDocumento, tipoDocumentoCodigo));
	}

	public Cliente findByNombre(String nombre) {
		return clienteRepository.findByNombreContainingIgnoreCase(nombre);
	}

	public Cliente save(Cliente cliente) {
		Long numeroDocumento = cliente.getNumeroDocumento();
		Long tipoDocumentoCodigo = cliente.getTipoDocumento().getCodigo();

		if (tipoDocumentoCodigo == 31 || tipoDocumentoCodigo == 50) {
			entidadValidator.validarNit(tipoDocumentoCodigo, numeroDocumento,
					Optional.ofNullable(cliente.getDigitoVerificacion()));
		}

		boolean existe = clienteRepository.existsByNumeroDocumentoAndTipoDocumento_Codigo(numeroDocumento,
				tipoDocumentoCodigo);
		entidadValidator.validarNoExistenciaEntidad(existe, numeroDocumento, tipoDocumentoCodigo, "cliente");

		return clienteRepository.save(cliente);
	}

	public Cliente update(Long numeroDocumento, Long tipoDocumentoCodigo, Cliente clienteActualizado) {
		TipoDocumento tipoDocumento = entidadValidator.obtenerTipoDocumento(tipoDocumentoCodigo);

		Cliente existente = clienteRepository.findByNumeroDocumentoAndTipoDocumento(numeroDocumento, tipoDocumento)
				.orElseThrow(() -> new ClienteNotFoundException(numeroDocumento, tipoDocumentoCodigo));

		if (!existente.getActivo()) {
			throw new ClienteInactivoException(numeroDocumento);
		}

		entidadValidator.validarNumeroDocumentoNoModificable(existente.getNumeroDocumento(),
				clienteActualizado.getNumeroDocumento(), "cliente");

		entidadValidator.validarTipoDocumentoNoModificable(existente.getTipoDocumento().getCodigo(),
				clienteActualizado.getTipoDocumento().getCodigo());

		
		entidadValidator.validarDigitoVerificacionNoModificable(Optional.ofNullable(existente.getDigitoVerificacion()),
				Optional.ofNullable(clienteActualizado.getDigitoVerificacion()), "cliente");

		existente.setNombre(clienteActualizado.getNombre());
		existente.setDireccion(clienteActualizado.getDireccion());
		existente.setTelefono(clienteActualizado.getTelefono());
		existente.setCorreoElectronico(clienteActualizado.getCorreoElectronico());
		existente.setTipoCliente(clienteActualizado.getTipoCliente());
		existente.setDigitoVerificacion(clienteActualizado.getDigitoVerificacion());

		return clienteRepository.save(existente);
	}

	public void disableByNumero(Long numeroDocumento, Long tipoDocumentoCodigo) {
		TipoDocumento tipoDocumento = entidadValidator.obtenerTipoDocumento(tipoDocumentoCodigo);

		Cliente cliente = clienteRepository.findByNumeroDocumentoAndTipoDocumento(numeroDocumento, tipoDocumento)
				.orElseThrow(() -> new ClienteNotFoundException(numeroDocumento, tipoDocumentoCodigo));

		cliente.setActivo(false);
		clienteRepository.save(cliente);
	}

	public void activarCliente(Long numeroDocumento, Long tipoDocumentoCodigo) {
		TipoDocumento tipoDocumento = entidadValidator.obtenerTipoDocumento(tipoDocumentoCodigo);

		Cliente cliente = clienteRepository.findByNumeroDocumentoAndTipoDocumento(numeroDocumento, tipoDocumento)
				.orElseThrow(() -> new ClienteNotFoundException(numeroDocumento, tipoDocumentoCodigo));

		if (!cliente.getActivo()) {
			cliente.setActivo(true);
			clienteRepository.save(cliente);
		}
	}
}
