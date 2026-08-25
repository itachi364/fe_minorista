package com.msvanegasg.facturaelectronica.bff.interfaces.rest;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffProperties;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffAuthProperties;
import com.msvanegasg.facturaelectronica.bff.infrastructure.security.BffCsrfFilter;
import com.msvanegasg.facturaelectronica.bff.infrastructure.security.BffSessionStore;
import com.msvanegasg.facturaelectronica.bff.infrastructure.security.BffUserSession;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
public class BffAuthController {

    public static final String SESSION_COOKIE = "NF_SESSION";
    public static final String OAUTH_ATTEMPT_COOKIE = "NF_OAUTH_ATTEMPT";

    private final BffAuthProperties properties;
    private final BffProperties serviceProperties;
    private final BffSessionStore sessionStore;
    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public BffAuthController(BffAuthProperties properties, BffProperties serviceProperties, BffSessionStore sessionStore,
            RestClient.Builder restClientBuilder) {
        this(properties, serviceProperties, sessionStore, restClientBuilder, new ObjectMapper().findAndRegisterModules());
    }

    BffAuthController(BffAuthProperties properties, BffProperties serviceProperties, BffSessionStore sessionStore,
            RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.properties = properties;
        this.serviceProperties = serviceProperties;
        this.sessionStore = sessionStore;
        this.restClientBuilder = restClientBuilder;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> session(HttpServletRequest request) {
        String csrf = token();
        String sessionId = cookieValue(request, SESSION_COOKIE);
        var session = sessionStore.findSession(sessionId);
        if (session.isPresent()) {
            BffUserSession userSession = session.get();
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, csrfCookie(csrf).toString())
                    .body(Map.of("authenticated", true, "authMode", properties.resolvedMode(), "csrfToken", csrf,
                            "userId", userSession.userId().toString(), "email", valueOrEmpty(userSession.email()),
                            "fullName", valueOrEmpty(userSession.fullName()),
                            "groups", userSession.groups(), "expiresAt", userSession.expiresAt().toString(),
                            "mfaAuthenticated", userSession.mfaAuthenticated()));
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, csrfCookie(csrf).toString())
                .body(Map.of("authenticated", false, "authMode", properties.resolvedMode(), "csrfToken", csrf));
    }

    @GetMapping("/login-url")
    public ResponseEntity<Map<String, String>> loginUrl() {
        if (!properties.isCognitoMode()) {
            return ResponseEntity.ok(Map.of("authMode", "local"));
        }
        String state = token();
        String nonce = token();
        String codeVerifier = token();
        String codeChallenge = codeChallenge(codeVerifier);
        String attemptId = sessionStore.createOAuthAttempt(state, nonce, codeVerifier);
        String url = properties.cognitoBaseUrl() + "/oauth2/authorize?response_type=code"
                + "&client_id=" + encode(properties.cognitoClientId())
                + "&redirect_uri=" + encode(properties.cognitoRedirectUri())
                + "&scope=" + encode("openid email profile")
                + "&state=" + encode(state)
                + "&nonce=" + encode(nonce)
                + "&code_challenge_method=S256"
                + "&code_challenge=" + encode(codeChallenge);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, oauthAttemptCookie(attemptId).toString())
                .body(Map.of("authMode", "cognito", "url", url, "state", state));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam String code, @RequestParam String state,
            @CookieValue(name = OAUTH_ATTEMPT_COOKIE, required = false) String attemptId) {
        if (!properties.isCognitoMode()) {
            return redirect(frontendRedirect("auth", "local"));
        }
        var attempt = sessionStore.consumeOAuthAttempt(attemptId, state);
        if (attempt.isEmpty()) {
            return redirect(frontendRedirect("auth", "invalid_state"), expiredCookie(OAUTH_ATTEMPT_COOKIE));
        }
        CognitoTokenResponse tokens = exchangeCode(code, attempt.get().codeVerifier());
        CognitoUserInfo userInfo = loadUserInfo(tokens.accessToken());
        InternalLoginResponse internalSession = issueInternalSession(userInfo);
        Instant expiresAt = min(Instant.now().plusSeconds(Math.max(60, tokens.expiresIn())),
                internalSession.expiresAt());
        String sessionId = sessionStore.createSession(new BffUserSession(internalSession.userId(), userInfo.subject(),
                internalSession.email(), internalSession.fullName(), stringRoles(internalSession.globalRoles()),
                internalSession.accessToken(), tokens.idToken(), tokens.refreshToken(), expiresAt, Instant.now(),
                mfaAuthenticated(tokens.idToken())));
        return redirect(frontendRedirect("auth", "success"), sessionCookie(sessionId, expiresAt),
                expiredCookie(OAUTH_ATTEMPT_COOKIE));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String sessionId = cookieValue(request, SESSION_COOKIE);
        sessionStore.findSession(sessionId).ifPresent(session -> {
            revokeIdentitySession(session);
            revokeCognitoRefreshToken(session);
        });
        sessionStore.revokeSession(sessionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie(SESSION_COOKIE).toString())
                .header(HttpHeaders.SET_COOKIE, expiredCookie(OAUTH_ATTEMPT_COOKIE).toString())
                .header(HttpHeaders.SET_COOKIE, expiredCookie(BffCsrfFilter.CSRF_COOKIE).toString())
                .body(Map.of("status", "LOGGED_OUT"));
    }

    private void revokeIdentitySession(BffUserSession session) {
        if (session.accessToken() == null || session.accessToken().isBlank()) {
            return;
        }
        try {
            identityClient()
                    .post()
                    .uri("/api/v1/auth/logout")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + session.accessToken())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            // La sesion BFF se invalida de todas formas; identity queda trazable por logs.
        }
    }

    private void revokeCognitoRefreshToken(BffUserSession session) {
        if (!properties.isCognitoMode() || session.refreshToken() == null || session.refreshToken().isBlank()) {
            return;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("token", session.refreshToken());
        form.add("client_id", properties.cognitoClientId());
        try {
            cognitoClient()
                    .post()
                    .uri("/oauth2/revoke")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            // The local BFF session is still revoked; upstream revocation will be traceable in service logs.
        }
    }

    private CognitoTokenResponse exchangeCode(String code, String codeVerifier) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.cognitoClientId());
        form.add("code", code);
        form.add("redirect_uri", properties.cognitoRedirectUri());
        form.add("code_verifier", codeVerifier);
        CognitoTokenResponse response = cognitoClient()
                .post()
                .uri("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(CognitoTokenResponse.class);
        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new IllegalStateException("Cognito token response did not include access token.");
        }
        return response;
    }

    private CognitoUserInfo loadUserInfo(String accessToken) {
        CognitoUserInfo response = cognitoClient()
                .get()
                .uri("/oauth2/userInfo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(CognitoUserInfo.class);
        if (response == null || response.subject() == null || response.subject().isBlank()) {
            throw new IllegalStateException("Cognito userInfo response did not include subject.");
        }
        return response;
    }

    private InternalLoginResponse issueInternalSession(CognitoUserInfo userInfo) {
        InternalLoginResponse response = identityClient()
                .post()
                .uri("/api/v1/internal/auth/cognito/session")
                .body(new CognitoSessionRequest(userInfo.subject(), userInfo.email(), userInfo.name(), userInfo.groups()))
                .retrieve()
                .body(InternalLoginResponse.class);
        if (response == null || response.userId() == null || response.accessToken() == null
                || response.accessToken().isBlank()) {
            throw new IllegalStateException("Identity service did not issue an internal session.");
        }
        return response;
    }

    private RestClient cognitoClient() {
        return restClientBuilder.clone().baseUrl(properties.cognitoBaseUrl()).build();
    }

    private RestClient identityClient() {
        return restClientBuilder.clone().baseUrl(serviceProperties.identityUrl()).build();
    }

    private ResponseCookie csrfCookie(String value) {
        return baseCookie(BffCsrfFilter.CSRF_COOKIE, value)
                .httpOnly(false)
                .build();
    }

    private ResponseCookie expiredCookie(String name) {
        return baseCookie(name, "")
                .maxAge(0)
                .build();
    }

    private ResponseCookie oauthAttemptCookie(String value) {
        return baseCookie(OAUTH_ATTEMPT_COOKIE, value)
                .httpOnly(true)
                .maxAge(Duration.ofMinutes(5))
                .build();
    }

    private ResponseCookie sessionCookie(String value, Instant expiresAt) {
        long seconds = Math.max(0, Duration.between(Instant.now(), expiresAt).toSeconds());
        return baseCookie(SESSION_COOKIE, value)
                .httpOnly(true)
                .maxAge(Duration.ofSeconds(seconds))
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String name, String value) {
        return ResponseCookie.from(name, value)
                .path("/")
                .secure(properties.cookieSecure() || properties.isProductionEnvironment())
                .sameSite(properties.resolvedSameSite());
    }

    private String token() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String codeChallenge(String codeVerifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required for PKCE.", exception);
        }
    }

    private static String cookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (var cookie : request.getCookies()) {
            if (name.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private URI frontendRedirect(String key, String value) {
        return UriComponentsBuilder.fromUriString(properties.resolvedFrontendBaseUrl())
                .queryParam(key, value)
                .build()
                .toUri();
    }

    private static ResponseEntity<Void> redirect(URI location, ResponseCookie... cookies) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.FOUND).location(location);
        for (ResponseCookie cookie : cookies) {
            builder.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return builder.build();
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private static Instant min(Instant first, Instant second) {
        if (second == null) {
            return first;
        }
        return first.isBefore(second) ? first : second;
    }

    private static Set<String> stringRoles(Set<String> roles) {
        return roles == null ? Set.of() : roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    private boolean mfaAuthenticated(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            return false;
        }
        String[] parts = idToken.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        try {
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode root = objectMapper.readTree(payload);
            JsonNode amr = root.path("amr");
            if (amr.isArray()) {
                for (JsonNode value : amr) {
                    String method = value.asText("");
                    if ("mfa".equalsIgnoreCase(method) || method.toLowerCase(java.util.Locale.ROOT).contains("mfa")) {
                        return true;
                    }
                }
            }
            String preferred = root.path("cognito:preferred_mfa_setting").asText("");
            return !preferred.isBlank();
        } catch (IllegalArgumentException | java.io.IOException exception) {
            return false;
        }
    }

    private record CognitoTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("id_token") String idToken,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("expires_in") long expiresIn) {
    }

    private record CognitoSessionRequest(String subject, String email, String fullName, Set<String> groups) {
    }

    private record InternalLoginResponse(UUID userId, String email, String fullName, String tokenType,
            String accessToken, Instant expiresAt, Set<String> globalRoles) {
    }

    private record CognitoUserInfo(
            @JsonProperty("sub") String subject,
            String email,
            String name,
            @JsonProperty("cognito:groups") Set<String> groups) {
        private CognitoUserInfo {
            groups = groups == null ? Set.of() : new HashSet<>(groups);
        }
    }
}
