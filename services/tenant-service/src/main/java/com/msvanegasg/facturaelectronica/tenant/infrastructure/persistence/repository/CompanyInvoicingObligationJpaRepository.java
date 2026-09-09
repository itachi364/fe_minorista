package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity.CompanyInvoicingObligationJpaEntity;

public interface CompanyInvoicingObligationJpaRepository
        extends JpaRepository<CompanyInvoicingObligationJpaEntity, UUID> {
    Optional<CompanyInvoicingObligationJpaEntity> findByCompanyIdAndCurrentTrue(UUID companyId);
    List<CompanyInvoicingObligationJpaEntity> findByCompanyIdOrderByVersionDesc(UUID companyId);

    @Query("select coalesce(max(s.version), 0) from CompanyInvoicingObligationJpaEntity s where s.companyId = :companyId")
    long maxVersion(@Param("companyId") UUID companyId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update CompanyInvoicingObligationJpaEntity s set s.current = false where s.companyId = :companyId and s.current = true")
    void clearCurrent(@Param("companyId") UUID companyId);
}
