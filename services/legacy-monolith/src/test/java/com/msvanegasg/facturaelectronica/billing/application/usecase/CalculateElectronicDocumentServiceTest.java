package com.msvanegasg.facturaelectronica.billing.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.billing.application.dto.CalculateElectronicDocumentCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.CalculatedElectronicDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentLineCalculationCommand;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

class CalculateElectronicDocumentServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID PRODUCT_ONE_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID PRODUCT_TWO_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");

    private final CalculateElectronicDocumentService service = new CalculateElectronicDocumentService();

    @Test
    void calculateDocumentTotalsFromRoundedLines() {
        CalculateElectronicDocumentCommand command = new CalculateElectronicDocumentCommand(
                COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_INVOICE,
                List.of(
                        new ElectronicDocumentLineCalculationCommand(
                                PRODUCT_ONE_ID,
                                new BigDecimal("2"),
                                new BigDecimal("15000"),
                                new BigDecimal("0"),
                                "IVA_19",
                                new BigDecimal("19")),
                        new ElectronicDocumentLineCalculationCommand(
                                PRODUCT_TWO_ID,
                                new BigDecimal("1"),
                                new BigDecimal("10000"),
                                new BigDecimal("1000"),
                                "IVA_5",
                                new BigDecimal("5"))));

        CalculatedElectronicDocumentResult result = service.calculate(command);

        assertThat(result.lines()).hasSize(2);
        assertThat(result.lines().get(0).grossAmount()).isEqualByComparingTo("30000.00");
        assertThat(result.lines().get(0).taxableAmount()).isEqualByComparingTo("30000.00");
        assertThat(result.lines().get(0).taxAmount()).isEqualByComparingTo("5700.00");
        assertThat(result.lines().get(0).lineTotal()).isEqualByComparingTo("35700.00");
        assertThat(result.lines().get(1).grossAmount()).isEqualByComparingTo("10000.00");
        assertThat(result.lines().get(1).discountAmount()).isEqualByComparingTo("1000.00");
        assertThat(result.lines().get(1).taxableAmount()).isEqualByComparingTo("9000.00");
        assertThat(result.lines().get(1).taxAmount()).isEqualByComparingTo("450.00");
        assertThat(result.lines().get(1).lineTotal()).isEqualByComparingTo("9450.00");
        assertThat(result.grossAmount()).isEqualByComparingTo("40000.00");
        assertThat(result.discountTotal()).isEqualByComparingTo("1000.00");
        assertThat(result.subtotal()).isEqualByComparingTo("39000.00");
        assertThat(result.taxTotal()).isEqualByComparingTo("6150.00");
        assertThat(result.total()).isEqualByComparingTo("45150.00");
    }

    @Test
    void calculateUsesHalfUpRoundingAtLineLevel() {
        CalculateElectronicDocumentCommand command = new CalculateElectronicDocumentCommand(
                COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_POS,
                List.of(new ElectronicDocumentLineCalculationCommand(
                        PRODUCT_ONE_ID,
                        new BigDecimal("1"),
                        new BigDecimal("100.025"),
                        new BigDecimal("0"),
                        "IVA_19",
                        new BigDecimal("19"))));

        CalculatedElectronicDocumentResult result = service.calculate(command);

        assertThat(result.lines().get(0).grossAmount()).isEqualByComparingTo("100.03");
        assertThat(result.lines().get(0).taxAmount()).isEqualByComparingTo("19.01");
        assertThat(result.total()).isEqualByComparingTo("119.04");
    }

    @Test
    void calculateRejectsDiscountGreaterThanGrossAmount() {
        CalculateElectronicDocumentCommand command = new CalculateElectronicDocumentCommand(
                COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_INVOICE,
                List.of(new ElectronicDocumentLineCalculationCommand(
                        PRODUCT_ONE_ID,
                        new BigDecimal("1"),
                        new BigDecimal("1000"),
                        new BigDecimal("1000.01"),
                        "IVA_19",
                        new BigDecimal("19"))));

        assertThatThrownBy(() -> service.calculate(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("discountAmount must be less than or equal to grossAmount");
    }

    @Test
    void calculateRejectsEmptyDocumentLines() {
        CalculateElectronicDocumentCommand command = new CalculateElectronicDocumentCommand(
                COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_INVOICE,
                List.of());

        assertThatThrownBy(() -> service.calculate(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("at least one line is required");
    }

    @Test
    void calculateRejectsNegativeTaxRate() {
        CalculateElectronicDocumentCommand command = new CalculateElectronicDocumentCommand(
                COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_INVOICE,
                List.of(new ElectronicDocumentLineCalculationCommand(
                        PRODUCT_ONE_ID,
                        new BigDecimal("1"),
                        new BigDecimal("1000"),
                        new BigDecimal("0"),
                        "IVA_19",
                        new BigDecimal("-1"))));

        assertThatThrownBy(() -> service.calculate(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("taxRate must be greater than or equal to zero");
    }
}
