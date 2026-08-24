package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.identity.application.port.out.UserAccountRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.domain.model.UserAccount;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.UserAccountJpaEntity;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository.UserAccountJpaRepository;

@Component
public class UserAccountPersistenceAdapter implements UserAccountRepositoryPort {

    private final UserAccountJpaRepository repository;

    public UserAccountPersistenceAdapter(UserAccountJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserAccount save(UserAccount user) {
        return toDomain(repository.save(toEntity(user)));
    }

    @Override
    public Optional<UserAccount> findById(UUID id) {
        return repository.findById(id).map(UserAccountPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return repository.findByEmail(email).map(UserAccountPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<UserAccount> findByCognitoSubject(String cognitoSubject) {
        return repository.findByCognitoSubject(cognitoSubject).map(UserAccountPersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, UUID id) {
        return repository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public List<UserAccount> findByCompanyIdAndEmailContaining(UUID companyId, String email) {
        String normalized = email == null || email.isBlank() ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
        List<UserAccountJpaEntity> users = normalized == null
                ? repository.findByCompanyId(companyId)
                : repository.findByCompanyIdAndEmailContaining(companyId, normalized);
        return users.stream()
                .map(UserAccountPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public long countByCompanyId(UUID companyId) {
        return repository.countByCompanyId(companyId);
    }

    private static UserAccountJpaEntity toEntity(UserAccount user) {
        UserAccountJpaEntity entity = new UserAccountJpaEntity();
        entity.setId(user.id());
        entity.setEmail(user.email());
        entity.setFullName(user.fullName());
        entity.setPasswordHash(user.passwordHash());
        entity.setCognitoSubject(user.cognitoSubject());
        entity.setStatus(user.status());
        entity.setCreatedAt(user.createdAt());
        entity.setUpdatedAt(user.updatedAt());
        return entity;
    }

    private static UserAccount toDomain(UserAccountJpaEntity entity) {
        return new UserAccount(entity.getId(), entity.getEmail(), entity.getFullName(), entity.getPasswordHash(),
                entity.getCognitoSubject(), entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
