package com.msvanegasg.facturaelectronica.thirdparty.application.port.in;

import java.util.List;

import com.msvanegasg.facturaelectronica.thirdparty.application.dto.CustomerCommand;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.CustomerResult;

public interface ManageCustomerUseCase {

    List<CustomerResult> findActive();

    List<CustomerResult> findInactive();

    List<CustomerResult> findByName(String name);

    CustomerResult findByDocument(Long documentTypeId, Long documentNumber);

    CustomerResult create(CustomerCommand command);

    CustomerResult update(Long documentTypeId, Long documentNumber, CustomerCommand command);

    void disable(Long documentTypeId, Long documentNumber);

    void enable(Long documentTypeId, Long documentNumber);
}
