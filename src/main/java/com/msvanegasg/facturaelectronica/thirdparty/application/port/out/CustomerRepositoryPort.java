package com.msvanegasg.facturaelectronica.thirdparty.application.port.out;

import java.util.List;
import java.util.Optional;

import com.msvanegasg.facturaelectronica.thirdparty.domain.model.Customer;

public interface CustomerRepositoryPort {

    List<Customer> findAllByActive(boolean active);

    List<Customer> findByNameContainingIgnoreCase(String name);

    Optional<Customer> findByDocumentTypeIdAndDocumentNumber(Long documentTypeId, Long documentNumber);

    boolean existsByDocumentNumberAndDocumentTypeId(Long documentNumber, Long documentTypeId);

    Customer save(Customer customer);
}
