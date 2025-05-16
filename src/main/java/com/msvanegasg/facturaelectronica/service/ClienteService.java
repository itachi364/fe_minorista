package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.DTO.ClienteDTO;
import com.msvanegasg.facturaelectronica.DTO.response.ClienteResponseDTO;
import com.msvanegasg.facturaelectronica.DTO.response.TipoDocumentoResponseDTO;
import com.msvanegasg.facturaelectronica.client.TipoDocumentoClient;
import com.msvanegasg.facturaelectronica.enums.TipoClienteEnum;
import com.msvanegasg.facturaelectronica.exception.cliente.*;
import com.msvanegasg.facturaelectronica.mapper.ClienteMapper;
import com.msvanegasg.facturaelectronica.models.Cliente;
import com.msvanegasg.facturaelectronica.repository.ClienteRepository;
import com.msvanegasg.facturaelectronica.validator.EntidadValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.msvanegasg.facturaelectronica.exception.tipodocumento.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteService {

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private ClienteMapper clienteMapper;

	@Autowired
	private EntidadValidator entidadValidator;

	@Autowired
	private TipoDocumentoClient tipoDocumentoClient;

	public ClienteResponseDTO crearCliente(ClienteDTO clienteDTO) {
		Long numeroDocumento = clienteDTO.getNumeroDocumento();
		Long idTipoDocumento = clienteDTO.getIdTipoDocumento();

		// Validar que no exista previamente
		validarExistenciaCliente(numeroDocumento, idTipoDocumento);

		// Determinar tipo de cliente
		TipoClienteEnum tipoCliente = entidadValidator.determinarTipoCliente(idTipoDocumento);

		// Validar NIT si aplica
		entidadValidator.validarNit(idTipoDocumento, numeroDocumento, clienteDTO.getDigitoVerificacion());

		// Obtener tipo de documento desde microservicio
		TipoDocumentoResponseDTO tipoDocumentoDTO = tipoDocumentoClient.obtenerTipoDocumentoPorCodigo(idTipoDocumento);

		// Mapear y construir cliente
		Cliente cliente = clienteMapper.toEntity(clienteDTO, tipoCliente);
		cliente.setActivo(true);

		Cliente clienteGuardado = clienteRepository.save(cliente);

		return clienteMapper.toResponseDTO(clienteGuardado, tipoDocumentoDTO.getId().toString(),
				tipoDocumentoDTO.getNombre());
	}

	public ClienteResponseDTO actualizarCliente(ClienteDTO clienteDTO, Long numeroDocumento, Long tipoDocumento) {
		// Validar existencia previa del cliente
		validarExistenciaCliente(numeroDocumento, tipoDocumento);

		// Obtener cliente existente
		Cliente clienteExistente = clienteRepository
				.findByIdTipoDocumentoAndNumeroDocumento(numeroDocumento, tipoDocumento)
				.orElseThrow(() -> new ClienteNotFoundException(numeroDocumento, tipoDocumento));
		
		//Valida si el Cliente esta activo
		if (Boolean.FALSE.equals(clienteExistente.getActivo())) {
			throw new ClienteInactivoException(numeroDocumento);
		}

		// Validar que el número de documento y tipo no hayan cambiado
		entidadValidator.validarNumeroDocumentoNoModificable(clienteExistente.getNumeroDocumento(),
				clienteDTO.getNumeroDocumento(), "cliente");
		entidadValidator.validarTipoDocumentoNoModificable(clienteExistente.getIdTipoDocumento(),
				clienteDTO.getIdTipoDocumento());
		entidadValidator.validarDigitoVerificacionNoModificable(
				Optional.ofNullable(clienteExistente.getDigitoVerificacion()), clienteDTO.getDigitoVerificacion(),
				"cliente");

		// Determinar tipo de cliente
		TipoClienteEnum tipoCliente = entidadValidator.determinarTipoCliente(clienteDTO.getIdTipoDocumento());
		if (tipoCliente == null) {
			throw new TipoClienteNoReconocidoException(clienteDTO.getIdTipoDocumento());
		}

		// Validar NIT
		entidadValidator.validarNit(clienteDTO.getIdTipoDocumento(), clienteDTO.getNumeroDocumento(),
				clienteDTO.getDigitoVerificacion());

		// Obtener tipo de documento desde microservicio
		TipoDocumentoResponseDTO tipoDocumentoDTO;
		try {
			tipoDocumentoDTO = tipoDocumentoClient.obtenerTipoDocumentoPorCodigo(tipoDocumento);
		} catch (Exception ex) {
			throw new TipoDocumentoNotFoundException(tipoDocumento);
		}

		// Mapear los cambios del DTO al cliente existente
		clienteMapper.actualizarEntidadDesdeDTO(clienteExistente, clienteDTO, tipoCliente);

		Cliente clienteActualizado = clienteRepository.save(clienteExistente);

		return clienteMapper.toResponseDTO(clienteActualizado, tipoDocumentoDTO.getId().toString(),
				tipoDocumentoDTO.getNombre());
	}

	public List<ClienteResponseDTO> listarClientesActivos() {
		return listarClientesPorEstado(true);
	}

	public List<ClienteResponseDTO> listarClientesInactivos() {
		return listarClientesPorEstado(false);
	}

	public List<ClienteResponseDTO> buscarPorNombre(String nombre) {

		return clienteRepository.findByNombreContainingIgnoreCase(nombre).stream().map(cliente -> {
			TipoDocumentoResponseDTO tipoDocumentoDTO = tipoDocumentoClient
					.obtenerTipoDocumentoPorCodigo(cliente.getIdTipoDocumento());
			return clienteMapper.toResponseDTO(cliente, tipoDocumentoDTO.getId().toString(),
					tipoDocumentoDTO.getNombre());
		}).collect(Collectors.toList());
	}

	public void eliminarCliente(Long numeroDocumento, Long tipoDocumentoId) {
		Cliente cliente = clienteRepository.findByIdTipoDocumentoAndNumeroDocumento(tipoDocumentoId, numeroDocumento)
				.orElseThrow(() -> new ClienteNotFoundException(numeroDocumento, tipoDocumentoId));
		cliente.setActivo(false);
		clienteRepository.save(cliente);
	}

	public void activarCliente(Long numeroDocumento, Long tipoDocumentoId) {
		Cliente cliente = clienteRepository.findByIdTipoDocumentoAndNumeroDocumento(tipoDocumentoId, numeroDocumento)
				.orElseThrow(() -> new ClienteNotFoundException(numeroDocumento, tipoDocumentoId));
		cliente.setActivo(true);
		clienteRepository.save(cliente);
	}

	public Cliente obtenerClientePorTipoYNumeroDocumento(Long tipoDocumentoId, Long numeroDocumento) {
		return clienteRepository.findByIdTipoDocumentoAndNumeroDocumento(tipoDocumentoId, numeroDocumento)
				.orElseThrow(() -> new ClienteNotFoundException(numeroDocumento, tipoDocumentoId));
	}

	private void validarExistenciaCliente(Long numeroDocumento, Long idTipoDocumento) {
		entidadValidator.validarNoExistenciaEntidad(
				!clienteRepository.existsByNumeroDocumentoAndIdTipoDocumento(numeroDocumento, idTipoDocumento),
				numeroDocumento, idTipoDocumento, "cliente");
	}

	private List<ClienteResponseDTO> listarClientesPorEstado(boolean activo) {
		return clienteRepository.findAllByActivo(activo).stream().map(cliente -> {
			TipoDocumentoResponseDTO tipoDocumentoDTO = tipoDocumentoClient
					.obtenerTipoDocumentoPorCodigo(cliente.getIdTipoDocumento());
			return clienteMapper.toResponseDTO(cliente, tipoDocumentoDTO.getId().toString(),
					tipoDocumentoDTO.getNombre());
		}).collect(Collectors.toList());
	}
}
