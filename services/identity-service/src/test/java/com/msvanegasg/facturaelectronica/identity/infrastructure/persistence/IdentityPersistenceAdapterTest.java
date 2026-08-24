package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msvanegasg.facturaelectronica.identity.domain.model.CompanyMembership;
import com.msvanegasg.facturaelectronica.identity.domain.model.RoleCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.UserAccount;
import com.msvanegasg.facturaelectronica.identity.domain.model.UserStatus;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.CompanyMembershipJpaEntity;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.UserAccountJpaEntity;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository.CompanyMembershipJpaRepository;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository.UserAccountJpaRepository;

@ExtendWith(MockitoExtension.class)
class IdentityPersistenceAdapterTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID MEMBERSHIP_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID COMPANY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Instant NOW = Instant.parse("2026-07-16T10:00:00Z");

    @Mock
    private UserAccountJpaRepository userRepository;

    @Mock
    private CompanyMembershipJpaRepository membershipRepository;

    @Test
    void savesAndFindsUserAccount() {
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(userEntity()));
        when(userRepository.findByCognitoSubject("cognito-subject")).thenReturn(Optional.of(userEntity()));
        UserAccountPersistenceAdapter adapter = new UserAccountPersistenceAdapter(userRepository);

        UserAccount saved = adapter.save(user());
        Optional<UserAccount> found = adapter.findByEmail("owner@example.com");
        Optional<UserAccount> foundBySubject = adapter.findByCognitoSubject("cognito-subject");

        assertThat(saved.id()).isEqualTo(USER_ID);
        assertThat(found).isPresent();
        assertThat(found.get().email()).isEqualTo("owner@example.com");
        assertThat(foundBySubject).isPresent();
        assertThat(foundBySubject.get().cognitoSubject()).isEqualTo("cognito-subject");
    }

    @Test
    void listsCompanyUsersWithoutEmailFilterUsingCompanyOnlyQuery() {
        when(userRepository.findByCompanyId(COMPANY_ID)).thenReturn(List.of(userEntity()));
        UserAccountPersistenceAdapter adapter = new UserAccountPersistenceAdapter(userRepository);

        List<UserAccount> users = adapter.findByCompanyIdAndEmailContaining(COMPANY_ID, "");

        assertThat(users).hasSize(1);
        assertThat(users.get(0).email()).isEqualTo("owner@example.com");
        verify(userRepository).findByCompanyId(COMPANY_ID);
        verify(userRepository, never()).findByCompanyIdAndEmailContaining(any(), any());
    }

    @Test
    void listsCompanyUsersWithNormalizedEmailFilter() {
        when(userRepository.findByCompanyIdAndEmailContaining(COMPANY_ID, "owner"))
                .thenReturn(List.of(userEntity()));
        UserAccountPersistenceAdapter adapter = new UserAccountPersistenceAdapter(userRepository);

        List<UserAccount> users = adapter.findByCompanyIdAndEmailContaining(COMPANY_ID, " Owner ");

        assertThat(users).hasSize(1);
        verify(userRepository).findByCompanyIdAndEmailContaining(COMPANY_ID, "owner");
        verify(userRepository, never()).findByCompanyId(any());
    }

    @Test
    void savesAndQueriesMembershipRoles() {
        when(membershipRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRepository.findByUserIdAndActiveTrue(USER_ID)).thenReturn(List.of(membershipEntity()));
        CompanyMembershipPersistenceAdapter adapter = new CompanyMembershipPersistenceAdapter(membershipRepository);

        CompanyMembership saved = adapter.save(membership());
        List<CompanyMembership> memberships = adapter.findByUserId(USER_ID);

        assertThat(saved.roles()).contains(RoleCode.OWNER);
        assertThat(memberships).hasSize(1);
        assertThat(memberships.get(0).companyId()).isEqualTo(COMPANY_ID);
    }

    private static UserAccount user() {
        return new UserAccount(USER_ID, "owner@example.com", "Owner User", "hashed", "cognito-subject",
                UserStatus.ACTIVE, NOW, NOW);
    }

    private static UserAccountJpaEntity userEntity() {
        UserAccountJpaEntity entity = new UserAccountJpaEntity();
        entity.setId(USER_ID);
        entity.setEmail("owner@example.com");
        entity.setFullName("Owner User");
        entity.setPasswordHash("hashed");
        entity.setCognitoSubject("cognito-subject");
        entity.setStatus(UserStatus.ACTIVE);
        entity.setCreatedAt(NOW);
        entity.setUpdatedAt(NOW);
        return entity;
    }

    private static CompanyMembership membership() {
        return new CompanyMembership(MEMBERSHIP_ID, COMPANY_ID, USER_ID, Set.of(RoleCode.OWNER), true, NOW, NOW);
    }

    private static CompanyMembershipJpaEntity membershipEntity() {
        CompanyMembershipJpaEntity entity = new CompanyMembershipJpaEntity();
        entity.setId(MEMBERSHIP_ID);
        entity.setCompanyId(COMPANY_ID);
        entity.setUserId(USER_ID);
        entity.setRoles(Set.of(RoleCode.OWNER));
        entity.setActive(true);
        entity.setCreatedAt(NOW);
        entity.setUpdatedAt(NOW);
        return entity;
    }
}
