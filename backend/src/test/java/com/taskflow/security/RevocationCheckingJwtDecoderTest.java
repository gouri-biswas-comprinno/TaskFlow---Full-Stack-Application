package com.taskflow.security;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import com.taskflow.persistence.RevokedTokenRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RevocationCheckingJwtDecoderTest {

    @Test
    void deniesTokenWhenRevocationStateCannotBeChecked() {
        Jwt token = new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "HS256"),
                Map.of("jti", "token-id", "sub", "user-id"));
        JwtDecoder delegate = ignored -> token;
        RevokedTokenRepository repository = mock(RevokedTokenRepository.class);
        when(repository.existsByJti("token-id"))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        RevocationCheckingJwtDecoder decoder = new RevocationCheckingJwtDecoder(delegate, repository);
        assertThrows(BadJwtException.class, () -> decoder.decode("token-value"));
    }
}
