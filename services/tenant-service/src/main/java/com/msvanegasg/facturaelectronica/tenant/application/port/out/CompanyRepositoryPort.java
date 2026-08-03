package com.msvanegasg.facturaelectronica.tenant.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.Company;

public interface CompanyRepositoryPort {

    Company save(Company company);

    Optional<Company> findById(UUID id);

    List<Company> findAll();

    boolean existsByIdentification(Integer identificationTypeCode, String identificationNumber);
}
