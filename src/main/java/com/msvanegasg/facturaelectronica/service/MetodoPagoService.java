package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.exception.MetodoPagoNotFoundException;
import com.msvanegasg.facturaelectronica.models.MetodoPago;
import com.msvanegasg.facturaelectronica.repository.MetodoPagoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetodoPagoService {

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    public List<MetodoPago> findAll() {
        return metodoPagoRepository.findAll();
    }
    
    public MetodoPago findActiveTrue() {
        return metodoPagoRepository.findByActivoTrue();
    }
    
    public MetodoPago findActiveFalse() {
        return metodoPagoRepository.findByActivoFalse();
    }

    public MetodoPago findById(Long id) {
        return metodoPagoRepository.findById(id)
                .orElseThrow(() -> new MetodoPagoNotFoundException(id));
    }

    public MetodoPago save(MetodoPago metodoPago) {
        return metodoPagoRepository.save(metodoPago);
    }

    public void disableById(Long id) {
        MetodoPago metodoPago = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new MetodoPagoNotFoundException(id));

        metodoPago.setActivo(false);
        metodoPagoRepository.save(metodoPago);
    }
    
    public void activarMetodo(Long id) {
        MetodoPago metodoPago = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new MetodoPagoNotFoundException(id));

        if (!metodoPago.getActivo()) {
        	metodoPago.setActivo(true);
        	metodoPagoRepository.save(metodoPago);
        }
    }
}
