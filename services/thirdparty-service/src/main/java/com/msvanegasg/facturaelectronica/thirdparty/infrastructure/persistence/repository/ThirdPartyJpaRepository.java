package com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdPartyRole;
import com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.entity.ThirdPartyJpaEntity;

public interface ThirdPartyJpaRepository extends JpaRepository<ThirdPartyJpaEntity, UUID> {

    Optional<ThirdPartyJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    Optional<ThirdPartyJpaEntity> findByCompanyIdAndIdentificationTypeCodeAndIdentificationNumber(UUID companyId,
            Integer identificationTypeCode, String identificationNumber);

    boolean existsByCompanyIdAndIdentificationTypeCodeAndIdentificationNumber(UUID companyId,
            Integer identificationTypeCode, String identificationNumber);

    @Query("""
            select distinct thirdParty
            from ThirdPartyJpaEntity thirdParty
            join thirdParty.roles role
            where thirdParty.companyId = :companyId
              and role = :role
              and (:active is null or thirdParty.active = :active)
            order by thirdParty.businessName asc, thirdParty.fullName asc
            """)
    List<ThirdPartyJpaEntity> findByCompanyIdAndRole(@Param("companyId") UUID companyId,
            @Param("role") ThirdPartyRole role, @Param("active") Boolean active);
}
