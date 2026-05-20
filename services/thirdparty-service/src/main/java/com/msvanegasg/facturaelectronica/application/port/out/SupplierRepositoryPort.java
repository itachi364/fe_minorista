package com.msvanegasg.facturaelectronica.thirdparty.application.port.out;

import java.util.List;
import java.util.Optional;

import com.msvanegasg.facturaelectronica.thirdparty.domain.model.Supplier;

public interface SupplierRepositoryPort {

    List<Supplier> findAll();

    List<Supplier> findActive();

    List<Supplier> findInactive();

    Optional<Supplier> findById(Long id);

    Optional<Supplier> findByDocumentNumberAndDocumentTypeId(Long documentNumber, Long documentTypeId);

    Optional<Supplier> findByNameContainingIgnoreCase(String name);

    boolean existsByDocumentNumber(Long documentNumber);

    Supplier save(Supplier supplier);
}
