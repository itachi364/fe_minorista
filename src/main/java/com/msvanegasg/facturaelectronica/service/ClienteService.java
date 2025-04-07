package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.exception.ClienteAlreadyExistsException;
import com.msvanegasg.facturaelectronica.exception.ClienteNotFoundException;
import com.msvanegasg.facturaelectronica.models.Cliente;
import com.msvanegasg.facturaelectronica.repository.ClienteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

	@Autowired
    private ClienteRepository clienteRepository;


    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    public List<Cliente> findActivos() {
        return clienteRepository.findByActivoTrue();
    }

    public Cliente findById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNotFoundException(id));
    }

    public Cliente findByNumeroDocumento(Long numeroDocumento) {
        return clienteRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new ClienteNotFoundException(numeroDocumento));
    }


    public List<Cliente> findByNombre(String nombre) {
        return clienteRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Cliente save(Cliente cliente) {
        if (clienteRepository.existsByNumeroDocumento(cliente.getNumeroDocumento())) {
            throw new ClienteAlreadyExistsException(cliente.getNumeroDocumento());
        }
        return clienteRepository.save(cliente);
    }
    
    public Cliente update(Long numero_documento, Cliente clienteActualizado) {
        Cliente existente = clienteRepository.findByNumeroDocumento(numero_documento)
                .orElseThrow(() -> new ClienteNotFoundException(numero_documento));

        // Si el número de documento cambia, validamos que no esté ya en uso por otro cliente
        if (!existente.getNumeroDocumento().equals(clienteActualizado.getNumeroDocumento())
                && clienteRepository.existsByNumeroDocumento(clienteActualizado.getNumeroDocumento())) {
            throw new ClienteAlreadyExistsException(clienteActualizado.getNumeroDocumento());
        }

        existente.setNombre(clienteActualizado.getNombre());
        existente.setTipoDocumento(clienteActualizado.getTipoDocumento());
        existente.setNumeroDocumento(clienteActualizado.getNumeroDocumento());
        existente.setDireccion(clienteActualizado.getDireccion());
        existente.setTelefono(clienteActualizado.getTelefono());
        existente.setCorreoElectronico(clienteActualizado.getCorreoElectronico());
        existente.setTipoCliente(clienteActualizado.getTipoCliente());

        return clienteRepository.save(existente);
    }


    public void disableById(Long id) {
        Cliente cliente = findById(id);
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }
}
