package com.msvanegasg.facturaelectronica.bff.interfaces.rest;

import java.net.URI;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyRequest;
import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyResponse;
import com.msvanegasg.facturaelectronica.bff.application.port.in.ProxyPublicApiUseCase;
import com.msvanegasg.facturaelectronica.bff.domain.model.BffRouteResolver;
import com.msvanegasg.facturaelectronica.bff.domain.model.TargetService;
import com.msvanegasg.facturaelectronica.bff.infrastructure.client.BffAccessDeniedException;
import com.msvanegasg.facturaelectronica.bff.infrastructure.security.BffSessionStore;
import com.msvanegasg.facturaelectronica.bff.infrastructure.security.BffSecurityAuditClient;
import com.msvanegasg.facturaelectronica.bff.infrastructure.security.BffUserSession;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class BffProxyController {

    private final ProxyPublicApiUseCase proxyUseCase;
    private final BffRouteResolver routeResolver;
    private final BffSessionStore sessionStore;
    private final BffSecurityAuditClient auditClient;

    public BffProxyController(ProxyPublicApiUseCase proxyUseCase, BffSessionStore sessionStore) {
        this(proxyUseCase, sessionStore, BffSecurityAuditClient.noop());
    }

    @Autowired
    public BffProxyController(ProxyPublicApiUseCase proxyUseCase, BffSessionStore sessionStore,
            BffSecurityAuditClient auditClient) {
        this.proxyUseCase = proxyUseCase;
        this.routeResolver = new BffRouteResolver();
        this.sessionStore = sessionStore;
        this.auditClient = auditClient;
    }

    @RequestMapping("/api/v1/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest servletRequest, @RequestBody(required = false) byte[] body) {
        String path = servletRequest.getRequestURI();
        TargetService targetService = routeResolver.resolve(path);
        URI targetUri = URI.create(path + queryString(servletRequest));
        HttpMethod method = HttpMethod.valueOf(servletRequest.getMethod());
        Optional<BffUserSession> session = serverSideSession(servletRequest);
        enforceMfaForCriticalMutation(method, path, servletRequest, session);
        ProxyResponse response = proxyUseCase.proxy(new ProxyRequest(targetService, method, targetUri,
                copyHeaders(servletRequest, session), body));
        return ResponseEntity.status(response.status())
                .headers(response.headers())
                .body(response.body());
    }

    private static String queryString(HttpServletRequest request) {
        String query = request.getQueryString();
        return query == null || query.isBlank() ? "" : "?" + query;
    }

    private HttpHeaders copyHeaders(HttpServletRequest request, Optional<BffUserSession> session) {
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(name -> headers.put(name,
                Collections.list(request.getHeaders(name))));
        attachServerSideSessionHeaders(headers, session);
        return headers;
    }

    private void attachServerSideSessionHeaders(HttpHeaders headers, Optional<BffUserSession> session) {
        if (headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            return;
        }
        session.ifPresent(userSession -> {
                    headers.setBearerAuth(userSession.accessToken());
                    headers.set("X-User-Id", userSession.userId().toString());
                });
    }

    private Optional<BffUserSession> serverSideSession(HttpServletRequest request) {
        if (request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
            return Optional.empty();
        }
        return sessionStore.findSession(cookieValue(request, BffAuthController.SESSION_COOKIE));
    }

    private void enforceMfaForCriticalMutation(HttpMethod method, String path, HttpServletRequest request,
            Optional<BffUserSession> session) {
        if (session.isEmpty() || session.get().mfaAuthenticated() || !isMutable(method)
                || !isCriticalPath(path)) {
            return;
        }
        auditClient.audit(request, session.get().userId(), "BFF_SECURITY", "MFA_REQUIRED", "FAILURE",
                "mfa_required");
        throw new BffAccessDeniedException("MFA is required for this critical action.");
    }

    private static boolean isMutable(HttpMethod method) {
        return HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method) || HttpMethod.PATCH.equals(method)
                || HttpMethod.DELETE.equals(method);
    }

    private static boolean isCriticalPath(String path) {
        String normalized = normalize(path);
        return normalized.equals("companies")
                || normalized.matches("companies/[^/]+(/(activate|suspend|branding|license).*)?")
                || normalized.startsWith("platform")
                || normalized.matches("companies/[^/]+/(memberships|users|roles|permissions)(/.*)?")
                || normalized.startsWith("catalog-definitions")
                || normalized.startsWith("catalogs")
                || normalized.startsWith("company-catalogs")
                || normalized.startsWith("dian-configuration")
                || normalized.startsWith("provider")
                || normalized.startsWith("issuers")
                || normalized.startsWith("numbering-resolutions")
                || normalized.startsWith("payroll");
    }

    private static String normalize(String path) {
        String value = path == null ? "" : path.strip().toLowerCase(Locale.ROOT);
        if (value.startsWith("/")) {
            value = value.substring(1);
        }
        if (value.startsWith("api/v1/")) {
            value = value.substring("api/v1/".length());
        }
        return value;
    }

    private static String cookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
