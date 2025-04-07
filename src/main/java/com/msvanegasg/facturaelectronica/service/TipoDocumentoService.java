package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.exception.TipoDocumentoNotFoundException;
import com.msvanegasg.facturaelectronica.models.TipoDocumento;
import com.msvanegasg.facturaelectronica.repository.TipoDocumentoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoDocumentoService {

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    public List<TipoDocumento> findAll() {
        return tipoDocumentoRepository.findAll();
    }
    
    public List<TipoDocumento> findActive() {
        return tipoDocumentoRepository.findByActivoTrue();
    }

    public TipoDocumento findById(Long id) {
        return tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new TipoDocumentoNotFoundException(id));
    }

    public TipoDocumento save(TipoDocumento tipoDocumento) {
        return tipoDocumentoRepository.save(tipoDocumento);
    }

    public void disableById(Long id) {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new TipoDocumentoNotFoundException(id));

        tipoDocumento.setActivo(false);
        tipoDocumentoRepository.save(tipoDocumento);
    }
}
