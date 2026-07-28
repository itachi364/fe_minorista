package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.UserAccountJpaEntity;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountJpaEntity, UUID> {

    Optional<UserAccountJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
