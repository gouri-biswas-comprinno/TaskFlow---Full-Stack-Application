package com.taskflow.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.persistence.RevokedTokenRepository;
import com.taskflow.security.RevocationCheckingJwtDecoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthenticationIntegrationTest {

    private static final String TEST_JWT_SECRET = "taskflow-integration-test-key-has-at-least-32-bytes";

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16.9-alpine")
            .withDatabaseName("taskflow_auth_test")
            .withUsername("taskflow_test")
            .withPassword("taskflow_test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("taskflow.jwt.secret", () -> TEST_JWT_SECRET);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Test
    void registrationNormalizesEmailAndRejectsDuplicatesWithoutReturningPassword() throws Exception {
        String email = uniqueEmail();
        String request = """
                {"email":"%s","password":"correct-horse"}
                """.formatted(email.toUpperCase());

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?",
                Integer.class,
                email));
        assertTrue(jdbcTemplate.queryForObject(
                "SELECT password_hash FROM users WHERE email = ?",
                String.class,
                email).startsWith("$2"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"email":"%s","password":"another-pass"}
                                """.formatted(email)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    void registrationRejectsPasswordsShorterThanEightCharacters() throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"email":"%s","password":"short7"}
                                """.formatted(email)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"));

        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?",
                Integer.class,
                email));
    }

    @Test
    void invalidLoginIsGenericAndValidLoginIssuesThirtyMinuteToken() throws Exception {
        String email = uniqueEmail();
        register(email, "correct-horse");

        String invalidEmailBody = """
                {"email":"%s","password":"wrong-password"}
                """.formatted(uniqueEmail());
        String wrongPasswordBody = """
                {"email":"%s","password":"wrong-password"}
                """.formatted(email);
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(invalidEmailBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(wrongPasswordBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"%s","password":"correct-horse"}
                                """.formatted(email.toUpperCase())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String accessToken = objectMapper.readTree(response)
                .get("accessToken")
                .asText();

        Jwt jwt = jwtDecoder.decode(accessToken);
        assertEquals("https://taskflow.local", jwt.getIssuer().toString());
        assertEquals(email, jdbcTemplate.queryForObject(
                "SELECT email FROM users WHERE id = ?",
                String.class,
                UUID.fromString(jwt.getSubject())));
        assertEquals(1800, jwt.getExpiresAt().getEpochSecond() - jwt.getIssuedAt().getEpochSecond());
        assertTrue(jwt.getId() != null && !jwt.getId().isBlank());
    }

    @Test
    void logoutRevokesTokenAndARecreatedDecoderStillRejectsIt() throws Exception {
        String email = uniqueEmail();
        register(email, "correct-horse");
        String accessToken = login(email, "correct-horse");
        Jwt jwt = jwtDecoder.decode(accessToken);

        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        assertTrue(revokedTokenRepository.existsByJti(jwt.getId()));
        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());

        NimbusJwtDecoder freshDelegate = NimbusJwtDecoder.withSecretKey(
                        new SecretKeySpec(TEST_JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        freshDelegate.setJwtValidator(JwtValidators.createDefaultWithIssuer("https://taskflow.local"));
        JwtDecoder recreatedDecoder = new RevocationCheckingJwtDecoder(freshDelegate, revokedTokenRepository);
        assertThrows(BadJwtException.class, () -> recreatedDecoder.decode(accessToken));
    }

    @Test
    void protectedRoutesRejectMissingMalformedExpiredWrongIssuerAndWrongSignatureTokens() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/tasks").header(
                        "Authorization",
                        "Bearer " + signedToken("https://taskflow.local", Instant.now().minusSeconds(60))))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/tasks").header(
                        "Authorization",
                        "Bearer " + signedToken("https://wrong.taskflow.local", Instant.now().plusSeconds(300))))
                .andExpect(status().isUnauthorized());

        String[] tokenParts = signedToken("https://taskflow.local", Instant.now().plusSeconds(300)).split("\\.");
        tokenParts[2] = (tokenParts[2].startsWith("A") ? "B" : "A") + tokenParts[2].substring(1);
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + String.join(".", tokenParts)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value(401));
    }

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isCreated());
    }

    private String login(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response)
                .get("accessToken")
                .asText();
    }

    private String signedToken(String issuer, Instant expiresAt) {
        Instant now = Instant.now();
        Instant issuedAt = expiresAt.isBefore(now) ? expiresAt.minusSeconds(300) : now.minusSeconds(1);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(UUID.randomUUID().toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims)).getTokenValue();
    }

    private String uniqueEmail() {
        return UUID.randomUUID() + "@example.com";
    }
}
