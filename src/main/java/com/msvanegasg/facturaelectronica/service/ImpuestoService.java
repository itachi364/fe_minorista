package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.DTO.ImpuestoDTO;
import com.msvanegasg.facturaelectronica.exception.impuesto.*;
import com.msvanegasg.facturaelectronica.mapper.ImpuestoMapper;
import com.msvanegasg.facturaelectronica.models.Impuesto;
import com.msvanegasg.facturaelectronica.models.Pais;
import com.msvanegasg.facturaelectronica.repository.ImpuestoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ImpuestoService {

	@Autowired
	private ImpuestoRepository impuestoRepository;

	public List<Impuesto> findAll() {
		return impuestoRepository.findAll();
	}

	public Impuesto findById(Long id) {
		return impuestoRepository.findById(id).orElseThrow(() -> new ImpuestoNotFoundException(id));
	}
	
	public Impuesto findByTipo(String tipo) {
		return impuestoRepository.findByTipo(tipo).orElseThrow(() -> new TipoImpuestoNotFoundException(tipo));
	}
	
	public Impuesto findByPorcentaje(BigDecimal porcentaje) {
		return impuestoRepository.findByPorcentaje(porcentaje).orElseThrow(() -> new IllegalArgumentException("No existe Impuesto con el porcentaje: "+porcentaje));
	}

	public Impuesto findActiveTrue() {
		return impuestoRepository.findByActivoTrue();
	}

	public Impuesto findActiveFalse() {
		return impuestoRepository.findByActivoFalse();
	}

	public Impuesto save(ImpuestoDTO impuestoDto, Pais pais) {
		Impuesto impuesto = ImpuestoMapper.toEntity(impuestoDto, pais);
		impuesto.setActivo(true);
		return impuestoRepository.save(impuesto);
	}

	public Impuesto actualizarImpuesto(Long id, ImpuestoDTO impuestoDTO, Pais pais) {
		Impuesto existente = impuestoRepository.findById(id).orElseThrow(() -> new ImpuestoNotFoundException(id));
		if (!existente.getActivo()) {
			throw new ImpuestoInactivoException(id);
		}
		existente.setNombre(impuestoDTO.getNombre());
		existente.setDescripcion(impuestoDTO.getDescripcion());
		existente.setTipo(impuestoDTO.getTipo());
		existente.setPorcentaje(impuestoDTO.getPorcentaje());
		existente.setPais(pais);
		return impuestoRepository.save(existente);
	}

	public void disableById(Long id) {
		Impuesto impuesto = impuestoRepository.findById(id).orElseThrow(() -> new ImpuestoNotFoundException(id));

		impuesto.setActivo(false);
		impuestoRepository.save(impuesto);
	}

	public void activarImpuesto(Long id) {
		Impuesto impuesto = impuestoRepository.findById(id).orElseThrow(() -> new ImpuestoNotFoundException(id));

		if (!impuesto.getActivo()) {
			impuesto.setActivo(true);
			impuestoRepository.save(impuesto);
		}
	}
}
