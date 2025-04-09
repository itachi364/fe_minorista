package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.exception.proveedor.*;
import com.msvanegasg.facturaelectronica.exception.proveedor.ProveedorNotFoundException;
import com.msvanegasg.facturaelectronica.models.Proveedor;
import com.msvanegasg.facturaelectronica.models.TipoDocumento;
import com.msvanegasg.facturaelectronica.repository.ProveedorRepository;
import com.msvanegasg.facturaelectronica.validator.EntidadValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService {

	@Autowired
	private ProveedorRepository proveedorRepository;

	@Autowired
	private EntidadValidator entidadValidator;

	public List<Proveedor> findAll() {
		return proveedorRepository.findAll();
	}

	public Proveedor findById(Long id) {
		return proveedorRepository.findById(id).orElseThrow(() -> new ProveedorNotFoundException(id));
	}

	public List<Proveedor> findActive() {
		return proveedorRepository.findByActivoTrue();
	}

	public List<Proveedor> findActiveFalse() {
		return proveedorRepository.findByActivoFalse();
	}

	public Proveedor findByNumeroDocumento(Long numeroDocumento, Long tipoDocumentoCodigo) {
		TipoDocumento tipoDocumento = entidadValidator.obtenerTipoDocumento(tipoDocumentoCodigo);
		return proveedorRepository.findByNumeroDocumentoAndTipoDocumento(numeroDocumento, tipoDocumento)
				.orElseThrow(() -> new ProveedorDocumentoNotFoundException(numeroDocumento, tipoDocumentoCodigo));
	}

	public Proveedor findByNombre(String nombre) {
		return proveedorRepository.findByNombreContainingIgnoreCase(nombre);
	}

	public Proveedor save(Proveedor proveedor) {
		Long numeroDocumento = proveedor.getNumeroDocumento();
		Long tipoDocumentoCodigo = proveedor.getTipoDocumento().getCodigo();

		if (tipoDocumentoCodigo == 31 || tipoDocumentoCodigo == 50) {
			entidadValidator.validarNit(tipoDocumentoCodigo, numeroDocumento,
					Optional.ofNullable(proveedor.getDigitoVerificacion()));
		}

		boolean existe = proveedorRepository.existsByNumeroDocumento(numeroDocumento);
		entidadValidator.validarNoExistenciaEntidad(existe, numeroDocumento, tipoDocumentoCodigo, "proveedor");

		return proveedorRepository.save(proveedor);
	}

	public Proveedor update(Long numeroDocumento, Long tipoDocumentoCodigo, Proveedor proveedorActualizado) {
		TipoDocumento tipoDocumento = entidadValidator.obtenerTipoDocumento(tipoDocumentoCodigo);
		Proveedor existente = proveedorRepository.findByNumeroDocumentoAndTipoDocumento(numeroDocumento, tipoDocumento)
				.orElseThrow(() -> new ProveedorDocumentoNotFoundException(numeroDocumento, tipoDocumentoCodigo));

		// Validar número de documento no modificable
		entidadValidator.validarNumeroDocumentoNoModificable(existente.getNumeroDocumento(),
				proveedorActualizado.getNumeroDocumento(), "proveedor");

		// Validar tipo de documento no modificable
		entidadValidator.validarTipoDocumentoNoModificable(existente.getTipoDocumento().getCodigo(),
				proveedorActualizado.getTipoDocumento().getCodigo());
		// Validar digito de verificación no modificable
		entidadValidator.validarDigitoVerificacionNoModificable(Optional.ofNullable(existente.getDigitoVerificacion()),
				Optional.ofNullable(proveedorActualizado.getDigitoVerificacion()), "proveedor");
		existente.setNombre(proveedorActualizado.getNombre());
		existente.setDireccion(proveedorActualizado.getDireccion());
		existente.setTelefono(proveedorActualizado.getTelefono());
		existente.setCorreoElectronico(proveedorActualizado.getCorreoElectronico());
		existente.setDigitoVerificacion(proveedorActualizado.getDigitoVerificacion());

		return proveedorRepository.save(existente);
	}

	public void disableByNumero(Long numeroDocumento, Long tipoDocumentoCodigo) {
		TipoDocumento tipoDocumento = entidadValidator.obtenerTipoDocumento(tipoDocumentoCodigo);
		Proveedor proveedor = proveedorRepository.findByNumeroDocumentoAndTipoDocumento(numeroDocumento, tipoDocumento)
				.orElseThrow(() -> new ProveedorDocumentoNotFoundException(numeroDocumento, tipoDocumentoCodigo));

		proveedor.setActivo(false);
		proveedorRepository.save(proveedor);
	}

	public void activarProveedor(Long numeroDocumento, Long tipoDocumentoCodigo) {
		TipoDocumento tipoDocumento = entidadValidator.obtenerTipoDocumento(tipoDocumentoCodigo);
		Proveedor proveedor = proveedorRepository.findByNumeroDocumentoAndTipoDocumento(numeroDocumento, tipoDocumento)
				.orElseThrow(() -> new ProveedorDocumentoNotFoundException(numeroDocumento, tipoDocumentoCodigo));

		if (!proveedor.getActivo()) {
			proveedor.setActivo(true);
			proveedorRepository.save(proveedor);
		}
	}
}
