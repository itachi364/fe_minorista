package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.PaymentMethodCode;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleStatus;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.SaleJpaEntity;

public interface SaleJpaRepository extends JpaRepository<SaleJpaEntity, UUID> {

    @EntityGraph(attributePaths = { "lines", "electronicDocument" })
    Optional<SaleJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);

    @EntityGraph(attributePaths = { "lines", "electronicDocument" })
    Optional<SaleJpaEntity> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey);

    @EntityGraph(attributePaths = { "lines", "electronicDocument" })
    @Query("""
            select s from SaleJpaEntity s
            left join s.electronicDocument d
            where s.companyId = :companyId
              and (:status is null or s.status = :status)
              and (:sellerId is null or s.createdBy = :sellerId)
              and (:customerId is null or s.customerId = :customerId)
              and (:paymentMethodCode is null or s.paymentMethodCode = :paymentMethodCode)
              and (:documentStatus is null or d.status = :documentStatus)
              and (:from is null or s.createdAt >= :from)
              and (:to is null or s.createdAt < :to)
            order by s.createdAt desc
            """)
    List<SaleJpaEntity> findSales(@Param("companyId") UUID companyId, @Param("status") SaleStatus status,
            @Param("sellerId") UUID sellerId, @Param("customerId") UUID customerId,
            @Param("paymentMethodCode") PaymentMethodCode paymentMethodCode,
            @Param("documentStatus") ElectronicDocumentStatus documentStatus,
            @Param("from") Instant from, @Param("to") Instant to);

    @EntityGraph(attributePaths = { "lines", "electronicDocument" })
    @Query("""
            select s from SaleJpaEntity s join s.electronicDocument d
            where s.companyId = :companyId
              and (:documentType is null or d.documentType = :documentType)
              and (:status is null or d.status = :status)
              and (:customerId is null or s.customerId = :customerId)
              and (:from is null or d.issuedAt >= :from)
              and (:to is null or d.issuedAt < :to)
              and (:prefix is null or d.prefix = :prefix)
              and (:number is null or d.documentNumber = :number)
              and (:cufeCude is null or d.cufeCude = :cufeCude)
            order by d.issuedAt desc
            """)
    List<SaleJpaEntity> findElectronicDocuments(@Param("companyId") UUID companyId,
            @Param("documentType") ElectronicDocumentType documentType, @Param("status") ElectronicDocumentStatus status,
            @Param("customerId") UUID customerId, @Param("from") Instant from, @Param("to") Instant to,
            @Param("prefix") String prefix, @Param("number") Long number, @Param("cufeCude") String cufeCude);

    @EntityGraph(attributePaths = { "lines", "electronicDocument" })
    @Query("select s from SaleJpaEntity s join s.electronicDocument d where s.companyId = :companyId and d.id = :documentId")
    Optional<SaleJpaEntity> findByCompanyIdAndElectronicDocumentId(@Param("companyId") UUID companyId,
            @Param("documentId") UUID documentId);

    @Query("""
            select count(d.id)
            from SaleJpaEntity s join s.electronicDocument d
            where s.companyId = :companyId
              and d.issuedAt >= :from
              and d.issuedAt < :to
            """)
    long countIssuedElectronicDocuments(@Param("companyId") UUID companyId, @Param("from") Instant from,
            @Param("to") Instant to);
}
