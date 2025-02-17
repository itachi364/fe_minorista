package com.msvanegasg.facturaelectronica.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.msvanegasg.facturaelectronica.models.Impuesto;
import com.msvanegasg.facturaelectronica.repository.ImpuestoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ImpuestoService {

    @Autowired
    private ImpuestoRepository impuestoRepository;

    public List<Impuesto> findAll() {
        return impuestoRepository.findAll();
    }

    public Optional<Impuesto> findById(Long id) {
        return impuestoRepository.findById(id);
    }

    public Impuesto save(Impuesto impuesto) {
        return impuestoRepository.save(impuesto);
    }

    public void disableById(Long id) {
    	impuestoRepository.findById(id).ifPresent(impuesto -> {
    		impuesto.setActivo(false);
    		impuestoRepository.save(impuesto);
        });
    }
}

