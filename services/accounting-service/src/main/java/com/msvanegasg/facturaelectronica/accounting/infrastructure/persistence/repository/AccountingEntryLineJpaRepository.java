package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountingEntryLineJpaEntity;

public interface AccountingEntryLineJpaRepository extends JpaRepository<AccountingEntryLineJpaEntity, UUID> {

    long countByAccountId(UUID accountId);

    List<AccountingEntryLineJpaEntity> findByEntryIdOrderByLineOrderAsc(UUID entryId);

    void deleteByEntryId(UUID entryId);
}
