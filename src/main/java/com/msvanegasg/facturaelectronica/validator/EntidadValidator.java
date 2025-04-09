package com.msvanegasg.facturaelectronica.validator;

import com.msvanegasg.facturaelectronica.models.TipoDocumento;
import com.msvanegasg.facturaelectronica.exception.cliente.*;
import com.msvanegasg.facturaelectronica.exception.proveedor.*;
import com.msvanegasg.facturaelectronica.exception.tipodocumento.*;
import com.msvanegasg.facturaelectronica.exception.util.*;
import com.msvanegasg.facturaelectronica.repository.TipoDocumentoRepository;
import com.msvanegasg.facturaelectronica.util.NitValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class EntidadValidator {

	@Autowired
	private TipoDocumentoRepository tipoDocumentoRepository;

	public TipoDocumento obtenerTipoDocumento(Long tipoDocumentoCodigo) {
		return tipoDocumentoRepository.findById(tipoDocumentoCodigo)
				.orElseThrow(() -> new TipoDocumentoNotFoundException(tipoDocumentoCodigo));
	}

	public void validarDigitoVerificacionNoModificable(Optional<Integer> dvActual, Optional<Integer> dvNuevo,
			String entidad) {

		if (!Objects.equals(dvActual.orElse(null), dvNuevo.orElse(null))) {
			throw new DigitoVerificacionNoModificableException(entidad);
		}
	}

	public void validarNumeroDocumentoNoModificable(Long original, Long actualizado, String nombreEntidad) {
		if (!original.equals(actualizado)) {
			switch (nombreEntidad.toLowerCase()) {
			case "cliente":
				throw new ClienteDocumentoNoModificableException(actualizado);
			case "proveedor":
				throw new ProveedorDocumentoNoModificableException(actualizado);
			default:
				throw new IllegalArgumentException("Entidad no soportada: " + nombreEntidad);
			}
		}
	}

	public void validarTipoDocumentoNoModificable(Long original, Long actualizado) {
		if (!original.equals(actualizado)) {
			throw new TipoDocumentoNoModificableException(actualizado);
		}
	}

	public void validarNit(Long tipoDocumentoCodigo, Long numeroDocumento, Optional<Integer> digitoVerificacion) {
		if (!NitValidatorUtil.esNitValido(tipoDocumentoCodigo, numeroDocumento, digitoVerificacion)) {
			throw new NitInvalidoException(numeroDocumento);
		}
	}

	public void validarNoExistenciaEntidad(boolean exists, Long numeroDocumento, Long tipoDocumentoCodigo,
			String nombreEntidad) {
		if (exists) {
			switch (nombreEntidad.toLowerCase()) {
			case "cliente":
				throw new ClienteAlreadyExistsException(numeroDocumento, tipoDocumentoCodigo);
			case "proveedor":
				throw new ProveedorAlreadyExistsException(numeroDocumento, tipoDocumentoCodigo);
			default:
				throw new IllegalArgumentException("Entidad no soportada: " + nombreEntidad);
			}
		}
	}
}
