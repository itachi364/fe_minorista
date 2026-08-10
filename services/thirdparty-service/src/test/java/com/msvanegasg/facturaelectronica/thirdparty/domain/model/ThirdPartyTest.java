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
                Set.of("O-13"), TaxRegime.RESPONSABLE_IVA, Set.of(ThirdPartyRole.CUSTOMER, ThirdPartyRole.SUPPLIER));

        assertThat(thirdParty.verificationDigit()).isEqualTo(8);
        assertThat(thirdParty.taxResponsibilities()).containsExactly("O-13");
        assertThat(thirdParty.taxRegime()).isEqualTo(TaxRegime.RESPONSABLE_IVA);
        assertThat(thirdParty.hasRole(ThirdPartyRole.CUSTOMER)).isTrue();
        assertThat(thirdParty.hasRole(ThirdPartyRole.SUPPLIER)).isTrue();
    }

    @Test
    void nonNitThirdPartyDoesNotAllowVerificationDigit() {
        assertThatThrownBy(() -> ThirdParty.restore(null, COMPANY_ID, PersonType.NATURAL, 13, "123456789", 1,
                "Persona Natural", null, null, null, null, null, null, Set.of("R-99-PN"), TaxRegime.NO_RESPONSABLE_IVA,
                Set.of(ThirdPartyRole.CUSTOMER), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("only applies to NIT");
    }

    @Test
    void simpleNaturalCustomerUsesAutomaticFiscalProfile() {
        ThirdParty thirdParty = ThirdParty.create(COMPANY_ID, PersonType.NATURAL, 13, "1234567890",
                "Cliente Natural", null, null, "cliente@example.com", null, null, "11001", null, null,
                Set.of(ThirdPartyRole.CUSTOMER));

        assertThat(thirdParty.verificationDigit()).isNull();
        assertThat(thirdParty.businessName()).isNull();
        assertThat(thirdParty.tradeName()).isNull();
        assertThat(thirdParty.taxResponsibilities()).containsExactly("R-99-PN");
        assertThat(thirdParty.taxRegime()).isEqualTo(TaxRegime.NO_RESPONSABLE_IVA);
    }

    @Test
    void simpleNaturalCustomerRejectsManualLegalEntityFieldsAndFiscalProfile() {
        assertThatThrownBy(() -> ThirdParty.create(COMPANY_ID, PersonType.NATURAL, 13, "1234567890",
                "Cliente Natural", "Cliente SAS", null, null, null, null, "11001", Set.of("R-99-PN"),
                TaxRegime.NO_RESPONSABLE_IVA, Set.of(ThirdPartyRole.CUSTOMER)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("businessName");

        assertThatThrownBy(() -> ThirdParty.create(COMPANY_ID, PersonType.NATURAL, 13, "1234567890",
                "Cliente Natural", null, null, null, null, null, "11001", Set.of("O-13"),
                TaxRegime.NO_RESPONSABLE_IVA, Set.of(ThirdPartyRole.CUSTOMER)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("R-99-PN");

        assertThatThrownBy(() -> ThirdParty.create(COMPANY_ID, PersonType.NATURAL, 13, "1234567890",
                "Cliente Natural", null, null, null, null, null, "11001", Set.of("R-99-PN"),
                TaxRegime.RESPONSABLE_IVA, Set.of(ThirdPartyRole.CUSTOMER)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NO_RESPONSABLE_IVA");
    }

    @Test
    void simpleNaturalCustomerRejectsNit() {
        assertThatThrownBy(() -> ThirdParty.create(COMPANY_ID, PersonType.NATURAL, 31, "900123456",
                "Cliente Natural", null, null, null, null, null, "11001", Set.of("R-99-PN"),
                TaxRegime.NO_RESPONSABLE_IVA, Set.of(ThirdPartyRole.CUSTOMER)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NIT");
    }

    @Test
    void juridicaRequiresBusinessName() {
        assertThatThrownBy(() -> ThirdParty.create(COMPANY_ID, PersonType.JURIDICA, 31, "900123456", null,
                null, null, null, null, null, null, Set.of("O-13"), TaxRegime.ORDINARIO,
                Set.of(ThirdPartyRole.SUPPLIER)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("businessName");
    }

    @Test
    void r99pnCannotBeCombinedWithOtherResponsibilities() {
        assertThatThrownBy(() -> ThirdParty.create(COMPANY_ID, PersonType.JURIDICA, 31, "900123456", null,
                "Cliente SAS", "Cliente", null, null, null, null, Set.of("R-99-PN", "O-13"),
                TaxRegime.NO_RESPONSABLE_IVA, Set.of(ThirdPartyRole.CUSTOMER)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("R-99-PN");
    }
}
