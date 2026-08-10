package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "catalog_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogItemJpaEntity {

    @EmbeddedId
    private CatalogItemJpaId id;

    @Column(name = "label", nullable = false, length = 180)
    private String label;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "regulatory", nullable = false)
    private Boolean regulatory;

    @Column(name = "source", nullable = false, length = 80)
    private String source;

    @Column(name = "source_version", nullable = false, length = 40)
    private String sourceVersion;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
