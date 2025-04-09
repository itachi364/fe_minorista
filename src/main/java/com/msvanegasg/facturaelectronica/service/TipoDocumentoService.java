package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.exception.tipodocumento.TipoDocumentoNotFoundException;
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
    
    public List<TipoDocumento> findActiveFalse() {
        return tipoDocumentoRepository.findByActivoFalse();
    }

    public TipoDocumento findById(Long codigo) {
        return tipoDocumentoRepository.findById(codigo)
                .orElseThrow(() -> new TipoDocumentoNotFoundException(codigo));
    }

    public TipoDocumento save(TipoDocumento tipoDocumento) {
        return tipoDocumentoRepository.save(tipoDocumento);
    }

    public void disableByCodigo(Long codigo) {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(codigo)
                .orElseThrow(() -> new TipoDocumentoNotFoundException(codigo));

        tipoDocumento.setActivo(false);
        tipoDocumentoRepository.save(tipoDocumento);
    }
    
    public void activarTipoDocumento(Long codigo) {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(codigo)
                .orElseThrow(() -> new TipoDocumentoNotFoundException(codigo));

        if (!tipoDocumento.getActivo()) {
        	tipoDocumento.setActivo(true);
        	tipoDocumentoRepository.save(tipoDocumento);
        }
    }
}
