package com.taskflow.security;

import org.springframework.dao.DataAccessException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import com.taskflow.persistence.RevokedTokenRepository;

public class RevocationCheckingJwtDecoder implements JwtDecoder {

    private final JwtDecoder delegate;
    private final RevokedTokenRepository revokedTokenRepository;

    public RevocationCheckingJwtDecoder(JwtDecoder delegate, RevokedTokenRepository revokedTokenRepository) {
        this.delegate = delegate;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Override
    public Jwt decode(String token) {
        Jwt jwt = delegate.decode(token);
        if (jwt.getId() == null || jwt.getId().isBlank()
                || jwt.getSubject() == null || jwt.getSubject().isBlank()
                || jwt.getIssuedAt() == null) {
            throw new BadJwtException("Required token claims are missing");
        }
        try {
            if (revokedTokenRepository.existsByJti(jwt.getId())) {
                throw new BadJwtException("Token has been revoked");
            }
        } catch (DataAccessException exception) {
            throw new BadJwtException("Token revocation status could not be verified", exception);
        }
        return jwt;
    }
}
