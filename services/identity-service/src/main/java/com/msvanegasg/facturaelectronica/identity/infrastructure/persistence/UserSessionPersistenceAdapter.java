package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.identity.application.port.out.UserSessionRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.domain.model.UserSession;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.UserSessionJpaEntity;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository.UserSessionJpaRepository;

@Component
public class UserSessionPersistenceAdapter implements UserSessionRepositoryPort {

    private final UserSessionJpaRepository repository;

    public UserSessionPersistenceAdapter(UserSessionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserSession save(UserSession session) {
        return toDomain(repository.save(toEntity(session)));
    }

    @Override
    public Optional<UserSession> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(UserSessionPersistenceAdapter::toDomain);
    }

    private static UserSessionJpaEntity toEntity(UserSession session) {
        UserSessionJpaEntity entity = new UserSessionJpaEntity();
        entity.setId(session.id());
        entity.setUserId(session.userId());
        entity.setTokenHash(session.tokenHash());
        entity.setExpiresAt(session.expiresAt());
        entity.setCreatedAt(session.createdAt());
        entity.setRevokedAt(session.revokedAt());
        return entity;
    }

    private static UserSession toDomain(UserSessionJpaEntity entity) {
        return new UserSession(entity.getId(), entity.getUserId(), entity.getTokenHash(), entity.getExpiresAt(),
                entity.getCreatedAt(), entity.getRevokedAt());
    }
}
