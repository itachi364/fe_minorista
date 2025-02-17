package com.msvanegasg.facturaelectronica.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.msvanegasg.facturaelectronica.models.TipoDocumento;
import com.msvanegasg.facturaelectronica.repository.TipoDocumentoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TipoDocumentoService {

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    public List<TipoDocumento> findAll() {
        return tipoDocumentoRepository.findAll();
    }

    public Optional<TipoDocumento> findById(Long id) {
        return tipoDocumentoRepository.findById(id);
    }

    public TipoDocumento save(TipoDocumento tipoDocumento) {
        return tipoDocumentoRepository.save(tipoDocumento);
    }

    public void disableById(Long id) {
        tipoDocumentoRepository.findById(id).ifPresent(tipoDocumento -> {
            tipoDocumento.setActivo(false);
            tipoDocumentoRepository.save(tipoDocumento);
        });
    }
}

