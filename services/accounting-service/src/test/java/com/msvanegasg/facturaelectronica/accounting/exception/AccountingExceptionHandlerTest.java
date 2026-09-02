package com.msvanegasg.facturaelectronica.accounting.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class AccountingExceptionHandlerTest {

    @Test
    void handleBusinessReturnsSpecificMessageForMissingReceivableRule() {
        AccountingExceptionHandler handler = new AccountingExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "test-correlation");

        var response = handler.handleBusiness(
                new IllegalStateException("accounting rule was not found: ACCOUNT_RECEIVABLE_REGISTERED"),
                request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(AccountingApiErrorCode.BUSINESS_RULE_VIOLATION);
        assertThat(response.getBody().message())
                .isEqualTo("Falta una regla contable activa para registrar la cuenta por cobrar.");
        assertThat(response.getBody().correlationId()).isEqualTo("test-correlation");
    }
}
