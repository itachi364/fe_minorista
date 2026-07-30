package com.msvanegasg.facturaelectronica.identity.application.port.out;

import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.GlobalRoleCode;

public interface GlobalUserRoleRepositoryPort {

    Set<GlobalRoleCode> findByUserId(UUID userId);

    boolean hasRole(UUID userId, GlobalRoleCode roleCode);

    void assignRole(UUID userId, GlobalRoleCode roleCode);
}