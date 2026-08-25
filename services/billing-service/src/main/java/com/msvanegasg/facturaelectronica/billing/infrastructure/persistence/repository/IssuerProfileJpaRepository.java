package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.IssuerProfileJpaEntity;

public interface IssuerProfileJpaRepository extends JpaRepository<IssuerProfileJpaEntity, UUID> {
    Optional<IssuerProfileJpaEntity> findFirstByCompanyIdAndActiveTrueOrderByIdDesc(UUID companyId);

    Optional<IssuerProfileJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    List<IssuerProfileJpaEntity> findByCompanyIdOrderByActiveDescLegalNameAsc(UUID companyId);

    @Modifying
    @Query("""
            update IssuerProfileJpaEntity issuer
               set issuer.active = false
             where issuer.companyId = :companyId
               and issuer.active = true
               and issuer.id <> :exceptId
            """)
    int deactivateActiveExcept(@Param("companyId") UUID companyId, @Param("exceptId") UUID exceptId);
}
