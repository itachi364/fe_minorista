package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity;

import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionScope;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "permission_catalog", schema = "identity")
public class PermissionCatalogJpaEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 80)
    private PermissionCode code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PermissionScope scope;

    @Column(nullable = false, length = 60)
    private String module;

    @Column(nullable = false, length = 250)
    private String description;

    @Column(nullable = false)
    private boolean active;

    public PermissionCatalogJpaEntity() {
    }

    public PermissionCode getCode() { return code; }
    public void setCode(PermissionCode code) { this.code = code; }
    public PermissionScope getScope() { return scope; }
    public void setScope(PermissionScope scope) { this.scope = scope; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
