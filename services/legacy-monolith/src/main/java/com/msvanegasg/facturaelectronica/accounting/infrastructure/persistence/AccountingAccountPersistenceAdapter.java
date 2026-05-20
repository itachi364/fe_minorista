package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountingAccountJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.AccountingAccountJpaRepository;

@Component
public class AccountingAccountPersistenceAdapter implements AccountRepositoryPort {

    private final AccountingAccountJpaRepository accountRepository;

    public AccountingAccountPersistenceAdapter(AccountingAccountJpaRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Optional<Account> findByCompanyIdAndCode(UUID companyId, String code) {
        return accountRepository.findByCompanyIdAndCode(companyId, code)
                .map(AccountingAccountPersistenceAdapter::toDomain);
    }

    @Override
    public Account save(Account account) {
        return toDomain(accountRepository.save(toEntity(account)));
    }

    private static Account toDomain(AccountingAccountJpaEntity entity) {
        return Account.restore(
                entity.getId(),
                entity.getCompanyId(),
                entity.getCode(),
                entity.getName(),
                entity.getParentAccountId(),
                Boolean.TRUE.equals(entity.getActive()));
    }

    private static AccountingAccountJpaEntity toEntity(Account account) {
        return AccountingAccountJpaEntity.builder()
                .id(account.id())
                .companyId(account.companyId())
                .code(account.code())
                .name(account.name())
                .category(account.category())
                .level(account.level())
                .nature(account.nature())
                .parentAccountId(account.parentAccountId())
                .active(account.active())
                .build();
    }
}
