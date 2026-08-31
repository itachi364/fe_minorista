package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseStatus;
import com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity.PurchaseJpaEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class PurchaseJpaRepositoryImpl implements PurchaseJpaRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<PurchaseJpaEntity> findPurchasesDynamic(UUID companyId, PurchaseStatus status, UUID supplierId,
            Instant from, Instant to) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PurchaseJpaEntity> query = builder.createQuery(PurchaseJpaEntity.class);
        Root<PurchaseJpaEntity> purchase = query.from(PurchaseJpaEntity.class);
        purchase.fetch("lines", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(purchase.get("companyId"), companyId));
        if (status != null) {
            predicates.add(builder.equal(purchase.get("status"), status));
        }
        if (supplierId != null) {
            predicates.add(builder.equal(purchase.get("supplierId"), supplierId));
        }
        if (from != null) {
            predicates.add(builder.greaterThanOrEqualTo(purchase.get("createdAt"), from));
        }
        if (to != null) {
            predicates.add(builder.lessThan(purchase.get("createdAt"), to));
        }

        query.select(purchase)
                .distinct(true)
                .where(predicates.toArray(Predicate[]::new))
                .orderBy(builder.desc(purchase.get("createdAt")));
        return entityManager.createQuery(query).getResultList();
    }
}
