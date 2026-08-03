package com.msvanegasg.facturaelectronica.thirdparty.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ThirdPartyTest {

    private static final UUID COMPANY_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Test
    void createsNitThirdPartyWithAutomaticVerificationDigit() {
        ThirdParty thirdParty = ThirdParty.create(COMPANY_ID, PersonType.JURIDICA, 31, "900123456", null,
                "Cliente SAS", "Cliente", "cliente@example.com", "3000000000", "Calle 1", "11001",
                Set.of(ThirdPartyRole.CUSTOMER, ThirdPartyRole.SUPPLIER));

        assertThat(thirdParty.verificationDigit()).isEqualTo(8);
        assertThat(thirdParty.hasRole(ThirdPartyRole.CUSTOMER)).isTrue();
        assertThat(thirdParty.hasRole(ThirdPartyRole.SUPPLIER)).isTrue();
    }

    @Test
    void nonNitThirdPartyDoesNotAllowVerificationDigit() {
        assertThatThrownBy(() -> ThirdParty.restore(null, COMPANY_ID, PersonType.NATURAL, 13, "123456789", 1,
                "Persona Natural", null, null, null, null, null, null, Set.of(ThirdPartyRole.CUSTOMER), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("only applies to NIT");
    }

    @Test
    void juridicaRequiresBusinessName() {
        assertThatThrownBy(() -> ThirdParty.create(COMPANY_ID, PersonType.JURIDICA, 31, "900123456", null,
                null, null, null, null, null, null, Set.of(ThirdPartyRole.SUPPLIER)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("businessName");
    }
}
