package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.exception.ImpuestoNotFoundException;
import com.msvanegasg.facturaelectronica.models.Impuesto;
import com.msvanegasg.facturaelectronica.repository.ImpuestoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImpuestoService {

    @Autowired
    private ImpuestoRepository impuestoRepository;

    public List<Impuesto> findAll() {
        return impuestoRepository.findAll();
    }

    public Impuesto findById(Long id) {
        return impuestoRepository.findById(id)
                .orElseThrow(() -> new ImpuestoNotFoundException(id));
    }
    
    public Impuesto findActiveTrue() {
        return impuestoRepository.findByActivoTrue();
    }
    
    public Impuesto findActiveFalse() {
        return impuestoRepository.findByActivoFalse();
    }

    public Impuesto save(Impuesto impuesto) {
        return impuestoRepository.save(impuesto);
    }

    public void disableById(Long id) {
        Impuesto impuesto = impuestoRepository.findById(id)
                .orElseThrow(() -> new ImpuestoNotFoundException(id));

        impuesto.setActivo(false);
        impuestoRepository.save(impuesto);
    }
    
    public void activarImpuesto(Long id) {
        Impuesto impuesto = impuestoRepository.findById(id)
                .orElseThrow(() -> new ImpuestoNotFoundException(id));

        if (!impuesto.getActivo()) {
        	impuesto.setActivo(true);
        	impuestoRepository.save(impuesto);
        }
    }
}
