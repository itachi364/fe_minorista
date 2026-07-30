package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.GlobalRoleCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "global_user_role", schema = "identity")
@IdClass(GlobalUserRoleId.class)
public class GlobalUserRoleJpaEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "role_code", nullable = false, length = 40)
    private GlobalRoleCode roleCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public GlobalUserRoleJpaEntity() {
    }

    public GlobalUserRoleJpaEntity(UUID userId, GlobalRoleCode roleCode, Instant createdAt) {
        this.userId = userId;
        this.roleCode = roleCode;
        this.createdAt = createdAt;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public GlobalRoleCode getRoleCode() { return roleCode; }
    public void setRoleCode(GlobalRoleCode roleCode) { this.roleCode = roleCode; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}