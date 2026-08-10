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
@Table(name = "municipality")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MunicipalityJpaEntity {

    @Id
    @Column(name = "municipality_code", nullable = false, length = 5)
    private String municipalityCode;

    @Column(name = "department_code", nullable = false, length = 2)
    private String departmentCode;

    @Column(name = "municipality_name", nullable = false, length = 160)
    private String municipalityName;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "source", nullable = false, length = 80)
    private String source;

    @Column(name = "source_version", nullable = false, length = 40)
    private String sourceVersion;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
