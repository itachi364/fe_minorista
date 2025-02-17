package com.msvanegasg.facturaelectronica.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.msvanegasg.facturaelectronica.models.MetodoPago;
import com.msvanegasg.facturaelectronica.repository.MetodoPagoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MetodoPagoService {

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    public List<MetodoPago> findAll() {
        return metodoPagoRepository.findAll();
    }

    public Optional<MetodoPago> findById(Long id) {
        return metodoPagoRepository.findById(id);
    }

    public MetodoPago save(MetodoPago metodoPago) {
        return metodoPagoRepository.save(metodoPago);
    }

    public void disableById(Long id) {
    	metodoPagoRepository.findById(id).ifPresent(metodoPago -> {
    		metodoPago.setActivo(false);
    		metodoPagoRepository.save(metodoPago);
        });
    }
}

