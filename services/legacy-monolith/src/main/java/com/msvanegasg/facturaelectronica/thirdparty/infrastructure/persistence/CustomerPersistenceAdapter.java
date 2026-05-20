package com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.CustomerRepositoryPort;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.Customer;
import com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.entity.CustomerJpaEntity;
import com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.repository.CustomerJpaRepository;

@Component
public class CustomerPersistenceAdapter implements CustomerRepositoryPort {

    private final CustomerJpaRepository customerRepository;

    public CustomerPersistenceAdapter(CustomerJpaRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Customer> findAllByActive(boolean active) {
        return customerRepository.findAllByActivo(active).stream()
                .map(CustomerPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Customer> findByNameContainingIgnoreCase(String name) {
        return customerRepository.findByNombreContainingIgnoreCase(name).stream()
                .map(CustomerPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<Customer> findByDocumentTypeIdAndDocumentNumber(Long documentTypeId, Long documentNumber) {
        return customerRepository.findByIdTipoDocumentoAndNumeroDocumento(documentTypeId, documentNumber)
                .map(CustomerPersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsByDocumentNumberAndDocumentTypeId(Long documentNumber, Long documentTypeId) {
        return customerRepository.existsByNumeroDocumentoAndIdTipoDocumento(documentNumber, documentTypeId);
    }

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity saved = customerRepository.save(toEntity(customer));
        return toDomain(saved);
    }

    private static Customer toDomain(CustomerJpaEntity entity) {
        return Customer.restore(
                entity.getIdCliente(),
                entity.getNombre(),
                entity.getIdTipoDocumento(),
                entity.getNumeroDocumento(),
                entity.getDigitoVerificacion(),
                entity.getDireccion(),
                entity.getTelefono(),
                entity.getCorreoElectronico(),
                entity.getTipoCliente(),
                Boolean.TRUE.equals(entity.getActivo()));
    }

    private static CustomerJpaEntity toEntity(Customer customer) {
        return CustomerJpaEntity.builder()
                .idCliente(customer.id())
                .nombre(customer.name())
                .idTipoDocumento(customer.documentTypeId())
                .numeroDocumento(customer.documentNumber())
                .digitoVerificacion(customer.verificationDigit())
                .direccion(customer.address())
                .telefono(customer.phone())
                .correoElectronico(customer.email())
                .tipoCliente(customer.customerType())
                .activo(customer.active())
                .build();
    }
}
