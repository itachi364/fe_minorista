package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msvanegasg.facturaelectronica.accounting.domain.model.ExpenseStatus;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.ExpenseJpaEntity;

public interface ExpenseJpaRepository extends JpaRepository<ExpenseJpaEntity, UUID> {

    Optional<ExpenseJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<ExpenseJpaEntity> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);

    @Query("""
            select e from ExpenseJpaEntity e
            where e.companyId = :companyId
              and (:status is null or e.status = :status)
              and (:supplierId is null or e.supplierId = :supplierId)
              and (:from is null or e.expenseDate >= :from)
              and (:to is null or e.expenseDate <= :to)
            order by e.expenseDate desc, e.createdAt desc
            """)
    List<ExpenseJpaEntity> findExpenses(@Param("companyId") UUID companyId,
            @Param("status") ExpenseStatus status, @Param("supplierId") UUID supplierId,
            @Param("from") LocalDate from, @Param("to") LocalDate to);
}
