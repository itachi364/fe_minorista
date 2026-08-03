package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.UserAccountJpaEntity;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountJpaEntity, UUID> {

    Optional<UserAccountJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);
    @Query("""
            select distinct u
            from UserAccountJpaEntity u
            where (:email is null or lower(u.email) like lower(concat('%', :email, '%')))
              and (
                exists (
                  select 1 from CompanyMembershipJpaEntity m
                  where m.userId = u.id and m.companyId = :companyId and m.active = true
                )
                or exists (
                  select 1 from CompanyUserRoleAssignmentJpaEntity a
                  where a.userId = u.id and a.companyId = :companyId and a.revokedAt is null
                )
              )
            order by u.email asc
            """)
    List<UserAccountJpaEntity> findByCompanyIdAndEmailContaining(@Param("companyId") UUID companyId,
            @Param("email") String email);
}
