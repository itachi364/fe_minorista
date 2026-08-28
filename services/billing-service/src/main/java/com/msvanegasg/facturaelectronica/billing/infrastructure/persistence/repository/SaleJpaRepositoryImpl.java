package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.PaymentMethodCode;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleStatus;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.ElectronicDocumentJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.SaleJpaEntity;
import com.msvanegasg.facturaelectronica.billing.infrastructure.persistence.entity.SaleLineJpaEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class SaleJpaRepositoryImpl implements SaleJpaRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<SaleJpaEntity> findElectronicDocumentsDynamic(UUID companyId, ElectronicDocumentType documentType,
            ElectronicDocumentStatus status, UUID customerId, Instant from, Instant to, String prefix, Long number,
            String cufeCude) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SaleJpaEntity> query = builder.createQuery(SaleJpaEntity.class);
        Root<SaleJpaEntity> sale = query.from(SaleJpaEntity.class);
        sale.fetch("lines", JoinType.LEFT);
        Fetch<SaleJpaEntity, ElectronicDocumentJpaEntity> documentFetch =
                sale.fetch("electronicDocument", JoinType.INNER);
        @SuppressWarnings("unchecked")
        Join<SaleJpaEntity, ElectronicDocumentJpaEntity> document = (Join<SaleJpaEntity, ElectronicDocumentJpaEntity>) documentFetch;

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(sale.get("companyId"), companyId));
        if (documentType != null) {
            predicates.add(builder.equal(document.get("documentType"), documentType));
        }
        if (status != null) {
            predicates.add(builder.equal(document.get("status"), status));
        }
        if (customerId != null) {
            predicates.add(builder.equal(sale.get("customerId"), customerId));
        }
        if (from != null) {
            predicates.add(builder.greaterThanOrEqualTo(document.get("issuedAt"), from));
        }
        if (to != null) {
            predicates.add(builder.lessThan(document.get("issuedAt"), to));
        }
        if (prefix != null) {
            predicates.add(builder.equal(document.get("prefix"), prefix));
        }
        if (number != null) {
            predicates.add(builder.equal(document.get("documentNumber"), number));
        }
        if (cufeCude != null) {
            predicates.add(builder.equal(document.get("cufeCude"), cufeCude));
        }

        query.select(sale)
                .distinct(true)
                .where(predicates.toArray(Predicate[]::new))
                .orderBy(builder.desc(document.get("issuedAt")));
        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<SaleJpaEntity> findSalesDynamic(UUID companyId, SaleStatus status, UUID sellerId, UUID customerId,
            UUID productId, PaymentMethodCode paymentMethodCode, ElectronicDocumentStatus documentStatus,
            Instant from, Instant to) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SaleJpaEntity> query = builder.createQuery(SaleJpaEntity.class);
        Root<SaleJpaEntity> sale = query.from(SaleJpaEntity.class);
        sale.fetch("lines", JoinType.LEFT);
        sale.fetch("electronicDocument", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(sale.get("companyId"), companyId));
        if (status != null) {
            predicates.add(builder.equal(sale.get("status"), status));
        }
        if (sellerId != null) {
            predicates.add(builder.equal(sale.get("createdBy"), sellerId));
        }
        if (customerId != null) {
            predicates.add(builder.equal(sale.get("customerId"), customerId));
        }
        if (productId != null) {
            Join<SaleJpaEntity, SaleLineJpaEntity> line = sale.join("lines", JoinType.INNER);
            predicates.add(builder.equal(line.get("productId"), productId));
        }
        if (paymentMethodCode != null) {
            predicates.add(builder.equal(sale.get("paymentMethodCode"), paymentMethodCode));
        }
        if (documentStatus != null) {
            Join<SaleJpaEntity, ElectronicDocumentJpaEntity> document = sale.join("electronicDocument", JoinType.LEFT);
            predicates.add(builder.equal(document.get("status"), documentStatus));
        }
        if (from != null) {
            predicates.add(builder.greaterThanOrEqualTo(sale.get("createdAt"), from));
        }
        if (to != null) {
            predicates.add(builder.lessThan(sale.get("createdAt"), to));
        }

        query.select(sale)
                .distinct(true)
                .where(predicates.toArray(Predicate[]::new))
                .orderBy(builder.desc(sale.get("createdAt")));
        return entityManager.createQuery(query).getResultList();
    }
}
