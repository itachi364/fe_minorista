package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
}

