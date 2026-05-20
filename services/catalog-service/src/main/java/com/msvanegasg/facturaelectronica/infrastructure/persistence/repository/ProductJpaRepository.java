package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CategoryJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.ProductJpaEntity;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductJpaEntity, Long> {

    List<ProductJpaEntity> findByCategoria(CategoryJpaEntity categoria);

    List<ProductJpaEntity> findByActivoTrue();

    List<ProductJpaEntity> findByActivoFalse();

    List<ProductJpaEntity> findByNombreContainingIgnoreCase(String nombre);

    Optional<ProductJpaEntity> findByCodigoBarras(Long codigoBarras);
}
