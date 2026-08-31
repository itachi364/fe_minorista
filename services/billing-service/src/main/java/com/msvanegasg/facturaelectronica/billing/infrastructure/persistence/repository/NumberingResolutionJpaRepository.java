package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalNoteType;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.NumberingResolutionJpaEntity;

public interface NumberingResolutionJpaRepository extends JpaRepository<NumberingResolutionJpaEntity, UUID> {
    Optional<NumberingResolutionJpaEntity> findFirstByCompanyIdAndDocumentTypeAndEnvironmentAndActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByValidToAsc(
            UUID companyId, ElectronicDocumentType documentType, FiscalEnvironment environment, LocalDate validFrom,
            LocalDate validTo);

    List<NumberingResolutionJpaEntity> findByCompanyIdOrderByValidToDesc(UUID companyId);

    List<NumberingResolutionJpaEntity> findByCompanyIdAndDocumentTypeOrderByValidToDesc(UUID companyId,
            ElectronicDocumentType documentType);

    List<NumberingResolutionJpaEntity> findByCompanyIdAndActiveOrderByValidToDesc(UUID companyId, boolean active);

    List<NumberingResolutionJpaEntity> findByCompanyIdAndDocumentTypeAndActiveOrderByValidToDesc(UUID companyId,
            ElectronicDocumentType documentType, boolean active);

    Optional<NumberingResolutionJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    @Modifying
    @Query("""
            update NumberingResolutionJpaEntity resolution
               set resolution.active = false
             where resolution.companyId = :companyId
               and resolution.documentType = :documentType
               and resolution.environment = :environment
               and resolution.active = true
               and resolution.id <> :exceptId
            """)
    int deactivateActiveSameScopeExcept(@Param("companyId") UUID companyId,
            @Param("documentType") ElectronicDocumentType documentType,
            @Param("environment") FiscalEnvironment environment,
            @Param("exceptId") UUID exceptId);

    @Query("""
            select count(d.id)
            from SaleJpaEntity s join s.electronicDocument d
            where d.companyId = :companyId
              and d.documentType = :documentType
              and d.prefix = :prefix
              and d.documentNumber between :fromNumber and :toNumber
            """)
    long countElectronicDocumentUsage(@Param("companyId") UUID companyId,
            @Param("documentType") ElectronicDocumentType documentType,
            @Param("prefix") String prefix,
            @Param("fromNumber") long fromNumber,
            @Param("toNumber") long toNumber);

    @Query("""
            select count(n.id)
            from FiscalNoteJpaEntity n
            where n.companyId = :companyId
              and n.noteType = :noteType
              and n.prefix = :prefix
              and n.documentNumber between :fromNumber and :toNumber
            """)
    long countFiscalNoteUsage(@Param("companyId") UUID companyId,
            @Param("noteType") FiscalNoteType noteType,
            @Param("prefix") String prefix,
            @Param("fromNumber") long fromNumber,
            @Param("toNumber") long toNumber);
}
