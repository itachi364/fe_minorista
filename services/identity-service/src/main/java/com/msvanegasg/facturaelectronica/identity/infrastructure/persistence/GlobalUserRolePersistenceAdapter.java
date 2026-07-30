package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.identity.application.port.out.GlobalUserRoleRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.domain.model.GlobalRoleCode;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.GlobalUserRoleJpaEntity;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository.GlobalUserRoleJpaRepository;

@Component
public class GlobalUserRolePersistenceAdapter implements GlobalUserRoleRepositoryPort {

    private final GlobalUserRoleJpaRepository repository;

    public GlobalUserRolePersistenceAdapter(GlobalUserRoleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Set<GlobalRoleCode> findByUserId(UUID userId) {
        return repository.findByUserId(userId).stream()
                .map(GlobalUserRoleJpaEntity::getRoleCode)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean hasRole(UUID userId, GlobalRoleCode roleCode) {
        return repository.existsByUserIdAndRoleCode(userId, roleCode);
    }

    @Override
    public void assignRole(UUID userId, GlobalRoleCode roleCode) {
        if (!hasRole(userId, roleCode)) {
            repository.save(new GlobalUserRoleJpaEntity(userId, roleCode, Instant.now()));
        }
    }
}