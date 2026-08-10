package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.DepartmentJpaEntity;

@Repository
public interface DepartmentJpaRepository extends JpaRepository<DepartmentJpaEntity, String> {

    List<DepartmentJpaEntity> findByActiveTrueOrderByDepartmentNameAsc();
}
