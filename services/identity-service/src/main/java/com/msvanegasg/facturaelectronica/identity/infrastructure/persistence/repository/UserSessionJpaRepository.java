package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.UserSessionJpaEntity;

public interface UserSessionJpaRepository extends JpaRepository<UserSessionJpaEntity, UUID> {

    Optional<UserSessionJpaEntity> findByTokenHash(String tokenHash);
}
