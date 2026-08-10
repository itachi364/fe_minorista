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
@Table(name = "department")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentJpaEntity {

    @Id
    @Column(name = "department_code", nullable = false, length = 2)
    private String departmentCode;

    @Column(name = "department_name", nullable = false, length = 120)
    private String departmentName;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "source", nullable = false, length = 80)
    private String source;

    @Column(name = "source_version", nullable = false, length = 40)
    private String sourceVersion;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
