package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CountryJpaEntity;

@Repository
public interface CountryJpaRepository extends JpaRepository<CountryJpaEntity, String> {

    List<CountryJpaEntity> findByNombreContainingIgnoreCase(String nombre);

    List<CountryJpaEntity> findByActivoTrue();

    List<CountryJpaEntity> findByActivoFalse();

    boolean existsByNombreIgnoreCase(String nombre);
}
