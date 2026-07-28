package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.identity.application.port.out.CompanyMembershipRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.domain.model.CompanyMembership;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.CompanyMembershipJpaEntity;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository.CompanyMembershipJpaRepository;

@Component
public class CompanyMembershipPersistenceAdapter implements CompanyMembershipRepositoryPort {

    private final CompanyMembershipJpaRepository repository;

    public CompanyMembershipPersistenceAdapter(CompanyMembershipJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public CompanyMembership save(CompanyMembership membership) {
        return toDomain(repository.save(toEntity(membership)));
    }

    @Override
    public Optional<CompanyMembership> findByIdAndCompanyId(java.util.UUID membershipId, java.util.UUID companyId) {
        return repository.findByIdAndCompanyId(membershipId, companyId)
                .map(CompanyMembershipPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<CompanyMembership> findByCompanyIdAndUserId(UUID companyId, UUID userId) {
        return repository.findByCompanyIdAndUserId(companyId, userId).map(CompanyMembershipPersistenceAdapter::toDomain);
    }

    @Override
    public List<CompanyMembership> findByUserId(UUID userId) {
        return repository.findByUserIdAndActiveTrue(userId).stream()
                .map(CompanyMembershipPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCompanyId(UUID companyId) {
        return repository.existsByCompanyId(companyId);
    }

    private static CompanyMembershipJpaEntity toEntity(CompanyMembership membership) {
        CompanyMembershipJpaEntity entity = new CompanyMembershipJpaEntity();
        entity.setId(membership.id());
        entity.setCompanyId(membership.companyId());
        entity.setUserId(membership.userId());
        entity.setRoles(membership.roles());
        entity.setActive(membership.active());
        entity.setCreatedAt(membership.createdAt());
        entity.setUpdatedAt(membership.updatedAt());
        return entity;
    }

    private static CompanyMembership toDomain(CompanyMembershipJpaEntity entity) {
        return new CompanyMembership(entity.getId(), entity.getCompanyId(), entity.getUserId(), entity.getRoles(),
                entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
