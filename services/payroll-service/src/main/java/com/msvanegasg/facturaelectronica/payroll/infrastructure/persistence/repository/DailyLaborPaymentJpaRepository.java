package com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.entity.DailyLaborPaymentJpaEntity;

public interface DailyLaborPaymentJpaRepository extends JpaRepository<DailyLaborPaymentJpaEntity, UUID> {
    List<DailyLaborPaymentJpaEntity> findByCompanyIdOrderByWorkDateDesc(UUID companyId);
    Optional<DailyLaborPaymentJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);
    List<DailyLaborPaymentJpaEntity> findByCompanyIdAndWorkDateBetweenOrderByWorkDateDesc(UUID companyId, LocalDate from, LocalDate to);
}
