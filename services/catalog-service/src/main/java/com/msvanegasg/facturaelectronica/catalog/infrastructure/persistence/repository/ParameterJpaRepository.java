package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.ParameterJpaEntity;

@Repository
public interface ParameterJpaRepository extends JpaRepository<ParameterJpaEntity, Long> {

    List<ParameterJpaEntity> findByActivoTrue();

    List<ParameterJpaEntity> findByActivoFalse();
}
