package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CategoryJpaEntity;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

    @Query(value = "SELECT * FROM categoria WHERE unaccent(lower(nombre)) LIKE concat('%', unaccent(lower(:nombre)), '%')", nativeQuery = true)
    List<CategoryJpaEntity> findByNombreIgnoreCaseAndAccent(@Param("nombre") String nombre);

    List<CategoryJpaEntity> findByActivoTrue();

    List<CategoryJpaEntity> findByActivoFalse();
}
