package com.msvanegasg.facturaelectronica.billing.infrastructure.messaging.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.msvanegasg.facturaelectronica.billing.infrastructure.messaging.entity.OutboxEventJpaEntity;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select event from OutboxEventJpaEntity event
            where event.status in :statuses
              and event.publishAttempts < :maxAttempts
            order by event.createdAt asc
            """)
    List<OutboxEventJpaEntity> findDispatchable(@Param("statuses") Collection<String> statuses,
            @Param("maxAttempts") int maxAttempts, Pageable pageable);
}