package com.msvanegasg.facturaelectronica.thirdparty.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class NitVerificationDigitTest {

    @Test
    void calculatesVerificationDigitUsingDianWeights() {
        assertThat(NitVerificationDigit.calculate("900123456")).isEqualTo(8);
        assertThat(NitVerificationDigit.calculate("800197268")).isEqualTo(4);
    }

    @Test
    void rejectsNonNumericDocumentNumber() {
        assertThatThrownBy(() -> NitVerificationDigit.calculate("900ABC456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("only digits");
    }
}
