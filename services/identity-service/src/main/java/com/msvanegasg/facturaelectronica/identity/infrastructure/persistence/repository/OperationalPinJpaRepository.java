package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.OperationalPinJpaEntity;

public interface OperationalPinJpaRepository extends JpaRepository<OperationalPinJpaEntity, UUID> {
}
