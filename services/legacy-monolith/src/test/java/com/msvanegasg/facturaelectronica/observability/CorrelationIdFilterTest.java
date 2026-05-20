package com.msvanegasg.facturaelectronica.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void propagatesIncomingCorrelationIdToResponseRequestAndMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/accounts");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(CorrelationId.HEADER_NAME, " corr-abc ");
        AtomicReference<String> mdcCorrelationId = new AtomicReference<>();
        AtomicReference<Object> requestAttribute = new AtomicReference<>();

        filter.doFilter(request, response, capturingChain(mdcCorrelationId, requestAttribute));

        assertThat(response.getHeader(CorrelationId.HEADER_NAME)).isEqualTo("corr-abc");
        assertThat(requestAttribute.get()).isEqualTo("corr-abc");
        assertThat(mdcCorrelationId.get()).isEqualTo("corr-abc");
        assertThat(MDC.get(CorrelationId.MDC_KEY)).isNull();
    }

    @Test
    void generatesCorrelationIdWhenHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/billing/pos-documents");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcCorrelationId = new AtomicReference<>();
        AtomicReference<Object> requestAttribute = new AtomicReference<>();

        filter.doFilter(request, response, capturingChain(mdcCorrelationId, requestAttribute));

        assertThat(response.getHeader(CorrelationId.HEADER_NAME)).isNotBlank();
        assertThat(requestAttribute.get()).isEqualTo(response.getHeader(CorrelationId.HEADER_NAME));
        assertThat(mdcCorrelationId.get()).isEqualTo(response.getHeader(CorrelationId.HEADER_NAME));
        assertThat(MDC.get(CorrelationId.MDC_KEY)).isNull();
    }

    private FilterChain capturingChain(
            AtomicReference<String> mdcCorrelationId,
            AtomicReference<Object> requestAttribute) {
        return new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                mdcCorrelationId.set(MDC.get(CorrelationId.MDC_KEY));
                requestAttribute.set(request.getAttribute(CorrelationId.REQUEST_ATTRIBUTE));
            }
        };
    }
}
