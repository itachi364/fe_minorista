package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.FiscalParameterJpaEntity;

public interface FiscalParameterJpaRepository extends JpaRepository<FiscalParameterJpaEntity, UUID> {
    @Query("""
            select parameter from FiscalParameterJpaEntity parameter
            where parameter.code = :code and parameter.published = true
              and parameter.validFrom <= :date
              and (parameter.validTo is null or parameter.validTo >= :date)
            order by parameter.validFrom desc
            """)
    List<FiscalParameterJpaEntity> findEffective(String code, LocalDate date);
}
