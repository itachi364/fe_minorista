package com.msvanegasg.facturaelectronica.thirdparty.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.thirdparty.application.dto.ThirdPartyCommand;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.ThirdPartyResult;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.ThirdPartyRepositoryPort;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.PersonType;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdParty;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdPartyRole;

class ThirdPartyManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID OTHER_COMPANY_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Test
    void createsThirdPartyWithAutomaticNitVerificationDigit() {
        InMemoryThirdPartyRepository repository = new InMemoryThirdPartyRepository();
        ThirdPartyManagementService service = new ThirdPartyManagementService(repository);

        ThirdPartyResult result = service.create(command(COMPANY_ID, Set.of(ThirdPartyRole.CUSTOMER)));

        assertThat(result.id()).isNotNull();
        assertThat(result.verificationDigit()).isEqualTo(8);
        assertThat(result.roles()).containsExactly(ThirdPartyRole.CUSTOMER);
        assertThat(result.active()).isTrue();
    }

    @Test
    void rejectsDuplicateDocumentWithinSameCompany() {
        InMemoryThirdPartyRepository repository = new InMemoryThirdPartyRepository();
        ThirdPartyManagementService service = new ThirdPartyManagementService(repository);
        service.create(command(COMPANY_ID, Set.of(ThirdPartyRole.CUSTOMER)));

        assertThatThrownBy(() -> service.create(command(COMPANY_ID, Set.of(ThirdPartyRole.SUPPLIER))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void allowsSameDocumentInDifferentCompanies() {
        InMemoryThirdPartyRepository repository = new InMemoryThirdPartyRepository();
        ThirdPartyManagementService service = new ThirdPartyManagementService(repository);
        service.create(command(COMPANY_ID, Set.of(ThirdPartyRole.CUSTOMER)));

        ThirdPartyResult result = service.create(command(OTHER_COMPANY_ID, Set.of(ThirdPartyRole.SUPPLIER)));

        assertThat(result.companyId()).isEqualTo(OTHER_COMPANY_ID);
    }

    @Test
    void findsByRoleAndKeepsTenantIsolation() {
        InMemoryThirdPartyRepository repository = new InMemoryThirdPartyRepository();
        ThirdPartyManagementService service = new ThirdPartyManagementService(repository);
        service.create(command(COMPANY_ID, Set.of(ThirdPartyRole.CUSTOMER, ThirdPartyRole.SUPPLIER)));
        service.create(command(OTHER_COMPANY_ID, Set.of(ThirdPartyRole.CUSTOMER)));

        List<ThirdPartyResult> customers = service.findByRole(COMPANY_ID, ThirdPartyRole.CUSTOMER, true);

        assertThat(customers).hasSize(1);
        assertThat(customers.get(0).companyId()).isEqualTo(COMPANY_ID);
    }

    private static ThirdPartyCommand command(UUID companyId, Set<ThirdPartyRole> roles) {
        return new ThirdPartyCommand(companyId, PersonType.JURIDICA, 31, "900123456", null, null,
                "Cliente SAS", "Cliente", "cliente@example.com", "3000000000", "Calle 1", "11001", roles);
    }

    private static final class InMemoryThirdPartyRepository implements ThirdPartyRepositoryPort {

        private final Map<UUID, ThirdParty> thirdParties = new LinkedHashMap<>();

        @Override
        public ThirdParty save(ThirdParty thirdParty) {
            UUID id = thirdParty.id() == null ? UUID.randomUUID() : thirdParty.id();
            ThirdParty toSave = ThirdParty.restore(id, thirdParty.companyId(), thirdParty.personType(),
                    thirdParty.identificationTypeCode(), thirdParty.identificationNumber(),
                    thirdParty.verificationDigit(), thirdParty.fullName(), thirdParty.businessName(),
                    thirdParty.tradeName(), thirdParty.email(), thirdParty.phone(), thirdParty.address(),
                    thirdParty.municipalityCode(), thirdParty.roles(), thirdParty.active());
            thirdParties.put(id, toSave);
            return toSave;
        }

        @Override
        public Optional<ThirdParty> findByCompanyIdAndId(UUID companyId, UUID id) {
            return Optional.ofNullable(thirdParties.get(id)).filter(thirdParty -> thirdParty.companyId().equals(companyId));
        }

        @Override
        public Optional<ThirdParty> findByCompanyIdAndDocument(UUID companyId, Integer identificationTypeCode,
                String identificationNumber) {
            return thirdParties.values().stream()
                    .filter(thirdParty -> thirdParty.companyId().equals(companyId))
                    .filter(thirdParty -> thirdParty.identificationTypeCode().equals(identificationTypeCode))
                    .filter(thirdParty -> thirdParty.identificationNumber().equals(identificationNumber))
                    .findFirst();
        }

        @Override
        public List<ThirdParty> findByCompanyIdAndRole(UUID companyId, ThirdPartyRole role, Boolean active) {
            return thirdParties.values().stream()
                    .filter(thirdParty -> thirdParty.companyId().equals(companyId))
                    .filter(thirdParty -> thirdParty.hasRole(role))
                    .filter(thirdParty -> active == null || thirdParty.active() == active)
                    .toList();
        }

        @Override
        public boolean existsByCompanyIdAndDocument(UUID companyId, Integer identificationTypeCode,
                String identificationNumber) {
            return findByCompanyIdAndDocument(companyId, identificationTypeCode, identificationNumber).isPresent();
        }
    }
}
