package com.msvanegasg.facturaelectronica.tenant.observability;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = CorrelationId.resolve(request.getHeader(CorrelationId.HEADER_NAME));
        long startNanos = System.nanoTime();

        request.setAttribute(CorrelationId.REQUEST_ATTRIBUTE, correlationId);
        response.setHeader(CorrelationId.HEADER_NAME, correlationId);
        MDC.put(CorrelationId.MDC_KEY, correlationId);

        try {
            LOGGER.info("event=http_request_start correlationId={} method={} path={}", correlationId,
                    request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            LOGGER.info("event=http_request_end correlationId={} method={} path={} status={} durationMs={}",
                    correlationId, request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);
            MDC.remove(CorrelationId.MDC_KEY);
        }
    }
}
