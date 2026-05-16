package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.BillingNumberingResolutionJpaEntity;

import jakarta.persistence.LockModeType;

public interface BillingNumberingResolutionJpaRepository
        extends JpaRepository<BillingNumberingResolutionJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BillingNumberingResolutionJpaEntity>
            findFirstByCompanyIdAndDocumentTypeAndEnvironmentAndActiveTrueAndValidFromLessThanEqualAndValidToGreaterThanEqualOrderByValidToAsc(
                    UUID companyId,
                    ElectronicDocumentType documentType,
                    FiscalEnvironment environment,
                    LocalDate validFrom,
                    LocalDate validTo);
}
