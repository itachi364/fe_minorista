package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "catalog_definition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogDefinitionJpaEntity {

    @Id
    @Column(name = "catalog_code", nullable = false, length = 80)
    private String catalogCode;

    @Column(name = "label", nullable = false, length = 180)
    private String label;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "regulatory", nullable = false)
    private Boolean regulatory;

    @Column(name = "company_configurable", nullable = false)
    private Boolean companyConfigurable;

    @Column(name = "global_editable_by_root", nullable = false)
    private Boolean globalEditableByRoot;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
