package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.entity.DianSubmissionEventJpaEntity;

public interface DianSubmissionEventJpaRepository extends JpaRepository<DianSubmissionEventJpaEntity, UUID> {
}
