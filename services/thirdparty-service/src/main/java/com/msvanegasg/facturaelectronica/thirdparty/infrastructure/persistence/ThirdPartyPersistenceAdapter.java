package com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.ThirdPartyRepositoryPort;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdParty;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdPartyRole;
import com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.entity.ThirdPartyJpaEntity;
import com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.repository.ThirdPartyJpaRepository;

@Component
public class ThirdPartyPersistenceAdapter implements ThirdPartyRepositoryPort {

    private final ThirdPartyJpaRepository repository;

    public ThirdPartyPersistenceAdapter(ThirdPartyJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ThirdParty save(ThirdParty thirdParty) {
        ThirdPartyJpaEntity saved = repository.save(toEntity(thirdParty));
        return toDomain(saved);
    }

    @Override
    public Optional<ThirdParty> findByCompanyIdAndId(UUID companyId, UUID id) {
        return repository.findByCompanyIdAndId(companyId, id).map(ThirdPartyPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<ThirdParty> findByCompanyIdAndDocument(UUID companyId, Integer identificationTypeCode,
            String identificationNumber) {
        return repository.findByCompanyIdAndIdentificationTypeCodeAndIdentificationNumber(companyId,
                identificationTypeCode, identificationNumber).map(ThirdPartyPersistenceAdapter::toDomain);
    }

    @Override
    public List<ThirdParty> findByCompanyIdAndRole(UUID companyId, ThirdPartyRole role, Boolean active) {
        return repository.findByCompanyIdAndRole(companyId, role, active).stream()
                .map(ThirdPartyPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCompanyIdAndDocument(UUID companyId, Integer identificationTypeCode,
            String identificationNumber) {
        return repository.existsByCompanyIdAndIdentificationTypeCodeAndIdentificationNumber(companyId,
                identificationTypeCode, identificationNumber);
    }

    private static ThirdParty toDomain(ThirdPartyJpaEntity entity) {
        return ThirdParty.restore(entity.getId(), entity.getCompanyId(), entity.getPersonType(),
                entity.getIdentificationTypeCode(), entity.getIdentificationNumber(), entity.getVerificationDigit(),
                entity.getFullName(), entity.getBusinessName(), entity.getTradeName(), entity.getEmail(),
                entity.getPhone(), entity.getAddress(), entity.getMunicipalityCode(), entity.getRoles(),
                Boolean.TRUE.equals(entity.getActive()));
    }

    private static ThirdPartyJpaEntity toEntity(ThirdParty thirdParty) {
        UUID id = thirdParty.id() == null ? UUID.randomUUID() : thirdParty.id();
        return ThirdPartyJpaEntity.builder()
                .id(id)
                .companyId(thirdParty.companyId())
                .personType(thirdParty.personType())
                .identificationTypeCode(thirdParty.identificationTypeCode())
                .identificationNumber(thirdParty.identificationNumber())
                .verificationDigit(thirdParty.verificationDigit())
                .fullName(thirdParty.fullName())
                .businessName(thirdParty.businessName())
                .tradeName(thirdParty.tradeName())
                .email(thirdParty.email())
                .phone(thirdParty.phone())
                .address(thirdParty.address())
                .municipalityCode(thirdParty.municipalityCode())
                .roles(new LinkedHashSet<>(thirdParty.roles()))
                .active(thirdParty.active())
                .build();
    }
}
