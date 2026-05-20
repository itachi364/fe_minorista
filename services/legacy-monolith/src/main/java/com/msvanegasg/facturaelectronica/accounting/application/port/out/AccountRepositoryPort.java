package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;

public interface AccountRepositoryPort {

    Optional<Account> findByCompanyIdAndCode(UUID companyId, String code);

    Account save(Account account);
}
