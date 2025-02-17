package com.msvanegasg.facturaelectronica.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.msvanegasg.facturaelectronica.models.Proveedor;
import com.msvanegasg.facturaelectronica.repository.ProveedorRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    public List<Proveedor> findAll() {
        return proveedorRepository.findAll();
    }

    public Optional<Proveedor> findById(Long id) {
        return proveedorRepository.findById(id);
    }

    public Proveedor save(Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }

    public void disableById(Long id) {
    	proveedorRepository.findById(id).ifPresent(proveedor -> {
    		proveedor.setActivo(false);
            proveedorRepository.save(proveedor);
        });
    }
}

