package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.exception.ClienteNotFoundException;
import com.msvanegasg.facturaelectronica.exception.ProveedorAlreadyExistsException;
import com.msvanegasg.facturaelectronica.exception.ProveedorNotFoundException;
import com.msvanegasg.facturaelectronica.models.Proveedor;
import com.msvanegasg.facturaelectronica.repository.ProveedorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    public List<Proveedor> findAll() {
        return proveedorRepository.findAll();
    }

    public Proveedor findById(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ProveedorNotFoundException(id));
    }
    
    public Proveedor findByNumeroDocumento(Long numeroDocumento) {
        return proveedorRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new ClienteNotFoundException(numeroDocumento));
    }

    public Proveedor save(Proveedor proveedor) {
    	if (proveedorRepository.existsByNumeroDocumento(proveedor.getNumeroDocumento())) {
            throw new ProveedorAlreadyExistsException(proveedor.getNumeroDocumento());
        }
        return proveedorRepository.save(proveedor);
    }
    
    public Proveedor update(Long numero_documento, Proveedor proveedorActualizado) {
        Proveedor existente = proveedorRepository.findByNumeroDocumento(numero_documento)
                .orElseThrow(() -> new ProveedorNotFoundException(numero_documento));

        // Si el número de documento cambia, validamos que no esté ya en uso por otro cliente
        if (!existente.getNumeroDocumento().equals(proveedorActualizado.getNumeroDocumento())
                && proveedorRepository.existsByNumeroDocumento(proveedorActualizado.getNumeroDocumento())) {
            throw new ProveedorAlreadyExistsException(proveedorActualizado.getNumeroDocumento());
        }

        existente.setNombre(proveedorActualizado.getNombre());
        existente.setTipoDocumento(proveedorActualizado.getTipoDocumento());
        existente.setNumeroDocumento(proveedorActualizado.getNumeroDocumento());
        existente.setDireccion(proveedorActualizado.getDireccion());
        existente.setTelefono(proveedorActualizado.getTelefono());
        existente.setCorreoElectronico(proveedorActualizado.getCorreoElectronico());

        return proveedorRepository.save(existente);
    }

    public void disableById(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ProveedorNotFoundException(id));

        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
    }
    
    public void activarProveedor(Long numeroDocumento) {
        Proveedor proveedor = proveedorRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new ProveedorNotFoundException(numeroDocumento));

        if (!proveedor.getActivo()) {
            proveedor.setActivo(true);
            proveedorRepository.save(proveedor);
        }
    }
}
