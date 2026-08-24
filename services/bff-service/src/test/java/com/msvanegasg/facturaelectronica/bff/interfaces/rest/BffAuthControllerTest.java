package com.msvanegasg.facturaelectronica.bff.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.mock.web.MockHttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffAuthProperties;
import com.msvanegasg.facturaelectronica.bff.infrastructure.security.BffEncryptedSessionStore;

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
        return new BffAuthController(properties, new BffEncryptedSessionStore(objectMapper, properties),
                RestClient.builder());
    }
}
