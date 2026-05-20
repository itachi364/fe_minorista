package com.msvanegasg.facturaelectronica.thirdparty.application.port.in;

import java.util.List;

import com.msvanegasg.facturaelectronica.thirdparty.application.dto.SupplierCommand;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.Supplier;

public interface ManageSupplierUseCase {

    List<Supplier> findAll();

    List<Supplier> findActive();

    List<Supplier> findInactive();

    Supplier findById(Long id);

    Supplier findByDocument(Long documentTypeId, Long documentNumber);

    Supplier findByName(String name);

    Supplier create(SupplierCommand command);

    Supplier update(Long documentTypeId, Long documentNumber, SupplierCommand command);

    void disable(Long documentTypeId, Long documentNumber);

    void enable(Long documentTypeId, Long documentNumber);
}
