package com.msvanegasg.facturaelectronica.inventory.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "service_supply_reference")
public class ServiceSupplyReferenceJpaEntity {

    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "service_product_id", nullable = false)
    private UUID serviceProductId;
    @Column(name = "supply_product_id", nullable = false)
    private UUID supplyProductId;
    @Column(length = 300)
    private String notes;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public UUID getServiceProductId() { return serviceProductId; }
    public void setServiceProductId(UUID serviceProductId) { this.serviceProductId = serviceProductId; }
    public UUID getSupplyProductId() { return supplyProductId; }
    public void setSupplyProductId(UUID supplyProductId) { this.supplyProductId = supplyProductId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
