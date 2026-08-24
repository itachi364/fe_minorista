package com.msvanegasg.facturaelectronica.bff.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.mock.web.MockHttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffProperties;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffAuthProperties;
import com.msvanegasg.facturaelectronica.bff.infrastructure.security.BffEncryptedSessionStore;
import com.msvanegasg.facturaelectronica.bff.infrastructure.security.BffUserSession;

import jakarta.servlet.http.Cookie;

class BffAuthControllerTest {

    @Test
    void returnsLocalModeWithoutOauthCookie() {
        BffAuthController controller = controller(localProperties());

        var response = controller.loginUrl();

        assertThat(response.getBody()).containsEntry("authMode", "local");
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).isNull();
    }

    @Test
    void createsCognitoAuthorizationUrlWithPkceAttemptCookie() {
        BffAuthController controller = controller(cognitoProperties());

        var response = controller.loginUrl();

        assertThat(response.getBody()).containsEntry("authMode", "cognito");
        assertThat(response.getBody()).containsKey("url");
        assertThat(response.getBody().get("url"))
                .contains("/oauth2/authorize")
                .contains("response_type=code")
                .contains("code_challenge_method=S256")
                .contains("client_id=client-id");
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE))
                .anySatisfy(cookie -> assertThat(cookie)
                        .contains(BffAuthController.OAUTH_ATTEMPT_COOKIE)
                        .contains("HttpOnly")
                        .contains("SameSite=Strict"));
    }

    @Test
    void logoutExpiresSessionOauthAndCsrfCookies() {
        BffAuthController controller = controller(cognitoProperties());

        var response = controller.logout(new MockHttpServletRequest());

        assertThat(response.getBody()).isEqualTo(Map.of("status", "LOGGED_OUT"));
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE))
                .anySatisfy(cookie -> assertThat(cookie).contains(BffAuthController.SESSION_COOKIE).contains("Max-Age=0"))
                .anySatisfy(cookie -> assertThat(cookie).contains(BffAuthController.OAUTH_ATTEMPT_COOKIE).contains("Max-Age=0"))
                .anySatisfy(cookie -> assertThat(cookie).contains("NF_CSRF").contains("Max-Age=0"));
    }

    @Test
    void logoutRevokesServerSideSession() {
        BffAuthProperties properties = localProperties();
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        BffEncryptedSessionStore sessionStore = new BffEncryptedSessionStore(objectMapper, properties);
        BffAuthController controller = new BffAuthController(properties, bffProperties(), sessionStore, RestClient.builder());
        String sessionId = sessionStore.createSession(new BffUserSession(java.util.UUID.randomUUID(), "subject", "user@example.com",
                "User Example", Set.of("OWNER"), "access", "id", "refresh",
                Instant.now().plusSeconds(300), Instant.now()));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(BffAuthController.SESSION_COOKIE, sessionId));

        controller.logout(request);

        assertThat(sessionStore.findSession(sessionId)).isEmpty();
    }

    @Test
    void logoutRevokesInternalIdentitySessionBestEffort() throws IOException {
        HttpServer identityServer = HttpServer.create(new InetSocketAddress(0), 0);
        AtomicReference<String> authorization = new AtomicReference<>();
        identityServer.createContext("/api/v1/auth/logout", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION));
            exchange.sendResponseHeaders(204, -1);
            try (OutputStream ignored = exchange.getResponseBody()) {
                // no body
            }
        });
        identityServer.start();
        try {
            BffAuthProperties properties = localProperties();
            ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
            BffEncryptedSessionStore sessionStore = new BffEncryptedSessionStore(objectMapper, properties);
            String identityUrl = "http://localhost:" + identityServer.getAddress().getPort();
            BffAuthController controller = new BffAuthController(properties,
                    new BffProperties("http://tenant", identityUrl, "http://catalog", "http://thirdparty",
                            "http://inventory", "http://billing", "http://accounting", "http://payroll",
                            "http://reporting", "http://dian", "http://audit"),
                    sessionStore, RestClient.builder());
            String sessionId = sessionStore.createSession(new BffUserSession(java.util.UUID.randomUUID(), "subject",
                    "user@example.com", "User Example", Set.of("OWNER"), "internal-access-token", "id", "refresh",
                    Instant.now().plusSeconds(300), Instant.now()));
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setCookies(new Cookie(BffAuthController.SESSION_COOKIE, sessionId));

            controller.logout(request);

            assertThat(authorization).hasValue("Bearer internal-access-token");
        } finally {
            identityServer.stop(0);
        }
    }

    private static BffAuthProperties localProperties() {
        return new BffAuthProperties("local", "local", "", "", "", "", "http://localhost:5173",
                "test-session-encryption-key-32-chars", false, "Strict", false);
    }

    private static BffAuthProperties cognitoProperties() {
        return new BffAuthProperties(
                "cognito",
                "prod",
                "https://auth.example.com",
                "client-id",
                "https://app.example.com/api/v1/auth/callback",
                "https://app.example.com",
                "https://app.example.com",
                "test-session-encryption-key-32-chars",
                true,
                "Strict",
                true);
    }

    private static BffAuthController controller(BffAuthProperties properties) {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        return new BffAuthController(properties, bffProperties(), new BffEncryptedSessionStore(objectMapper, properties),
                RestClient.builder());
    }

    private static BffProperties bffProperties() {
        String url = "http://localhost";
        return new BffProperties(url, url, url, url, url, url, url, url, url, url, url);
    }
}
