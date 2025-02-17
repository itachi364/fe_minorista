package com.msvanegasg.facturaelectronica.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.msvanegasg.facturaelectronica.models.TipoGasto;
import com.msvanegasg.facturaelectronica.repository.TipoGastoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TipoGastoService {

	@Autowired
	private TipoGastoRepository tipoGastoRepository;

	public List<TipoGasto> findAll() {
		return tipoGastoRepository.findAll();
	}

	public Optional<TipoGasto> findById(Long id) {
		return tipoGastoRepository.findById(id);
	}

	public TipoGasto save(TipoGasto tipoGasto) {
		return tipoGastoRepository.save(tipoGasto);
	}

	public void disableById(Long id) {
		tipoGastoRepository.findById(id).ifPresent(tipoGasto -> {
			tipoGasto.setActivo(false);
            tipoGastoRepository.save(tipoGasto);
        });
    }
}
