package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.DTO.GastoDTO;
import com.msvanegasg.facturaelectronica.enums.Estado;
import com.msvanegasg.facturaelectronica.exception.*;
import com.msvanegasg.facturaelectronica.exception.gasto.*;
import com.msvanegasg.facturaelectronica.mapper.GastoMapper;
import com.msvanegasg.facturaelectronica.models.Gasto;
import com.msvanegasg.facturaelectronica.models.MetodoPago;
import com.msvanegasg.facturaelectronica.models.TipoGasto;
import com.msvanegasg.facturaelectronica.repository.GastoRepository;
import com.msvanegasg.facturaelectronica.repository.MetodoPagoRepository;
import com.msvanegasg.facturaelectronica.repository.TipoGastoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GastoService {

	private final GastoRepository gastoRepository;
	private final TipoGastoRepository tipoGastoRepository;
	private final MetodoPagoRepository metodoPagoRepository;

	public Gasto crearGasto(GastoDTO gastoDTO) {
		TipoGasto tipoGasto = tipoGastoRepository.findById(gastoDTO.getIdTipoGasto())
				.orElseThrow(() -> new TipoGastoNotFoundException(gastoDTO.getIdTipoGasto()));

		MetodoPago metodoPago = metodoPagoRepository.findById(gastoDTO.getIdMetodoPago())
				.orElseThrow(() -> new MetodoPagoNotFoundException(gastoDTO.getIdMetodoPago()));

		Gasto gasto = GastoMapper.toEntity(gastoDTO, tipoGasto, metodoPago);
		gasto.setActivo(true);

		return gastoRepository.save(gasto);
	}

	public Gasto actualizarGasto(Long id, GastoDTO gastoDTO) {
		Gasto existente = gastoRepository.findById(id).orElseThrow(() -> new GastoNotFoundException(id));

		if (!existente.getActivo()) {
			throw new GastoInactivoException(id);
		}

		TipoGasto tipoGasto = tipoGastoRepository.findById(gastoDTO.getIdTipoGasto())
				.orElseThrow(() -> new TipoGastoNotFoundException(gastoDTO.getIdTipoGasto()));

		MetodoPago metodoPago = metodoPagoRepository.findById(gastoDTO.getIdMetodoPago())
				.orElseThrow(() -> new MetodoPagoNotFoundException(gastoDTO.getIdMetodoPago()));

		existente.setFecha(gastoDTO.getFecha());
		existente.setMonto(gastoDTO.getMonto());
		existente.setDescripcion(gastoDTO.getDescripcion());
		existente.setTipoGasto(tipoGasto);
		existente.setMetodoPago(metodoPago);
		existente.setUrlEvidencia(gastoDTO.getUrlEvidencia());
		existente.setEstado(gastoDTO.getEstado());

		return gastoRepository.save(existente);
	}

	public void eliminarGasto(Long id) {
		Gasto gasto = gastoRepository.findById(id).orElseThrow(() -> new GastoNotFoundException(id));

		gasto.setActivo(false);
		gastoRepository.save(gasto);
	}

	public Gasto obtenerGastoPorId(Long id) {
		return gastoRepository.findById(id).orElseThrow(() -> new GastoNotFoundException(id));
	}

	public List<Gasto> listarGastosActivos() {
		return gastoRepository.findByActivo(true);
	}

	public List<Gasto> listarGastosPorEstado(Estado estado) {
		return gastoRepository.findByEstado(estado.name());
	}

	public List<Gasto> listarGastosPorTipo(Long idTipoGasto) {
		return gastoRepository.findByTipoGastoIdTipoGasto(idTipoGasto);
	}

	public List<Gasto> listarGastosPorMetodoPago(Long idMetodoPago) {
		return gastoRepository.findByMetodoPagoIdMetodoPago(idMetodoPago);
	}

	public Gasto findByDescripcion(String descripcion) {
		return gastoRepository.findByDescripcionContainingIgnoreCase(descripcion);
	}
}
