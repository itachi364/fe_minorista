package com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.entity.PayrollSettingsJpaEntity;

public interface PayrollSettingsJpaRepository extends JpaRepository<PayrollSettingsJpaEntity, UUID> {
}
