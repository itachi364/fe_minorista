package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.exception.TipoGastoNotFoundException;
import com.msvanegasg.facturaelectronica.models.TipoGasto;
import com.msvanegasg.facturaelectronica.repository.TipoGastoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoGastoService {

    @Autowired
    private TipoGastoRepository tipoGastoRepository;

    public List<TipoGasto> findAll() {
        return tipoGastoRepository.findAll();
    }

    public TipoGasto findById(Long id) {
        return tipoGastoRepository.findById(id)
                .orElseThrow(() -> new TipoGastoNotFoundException(id));
    }

    public TipoGasto save(TipoGasto tipoGasto) {
        return tipoGastoRepository.save(tipoGasto);
    }

    public void disableById(Long id) {
        TipoGasto tipoGasto = tipoGastoRepository.findById(id)
                .orElseThrow(() -> new TipoGastoNotFoundException(id));

        tipoGasto.setActivo(false);
        tipoGastoRepository.save(tipoGasto);
    }
}
