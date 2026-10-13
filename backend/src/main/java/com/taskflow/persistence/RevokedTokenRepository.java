package com.taskflow.persistence;

import java.sql.Timestamp;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RevokedTokenRepository {

    private final JdbcTemplate jdbcTemplate;

    public RevokedTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(RevokedTokenRecord token) {
        jdbcTemplate.update(
                "INSERT INTO revoked_tokens (jti, expires_at, revoked_at) VALUES (?, ?, ?)",
                token.jti(),
                Timestamp.from(token.expiresAt()),
                Timestamp.from(token.revokedAt()));
    }

    public boolean existsByJti(String jti) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM revoked_tokens WHERE jti = ?",
                Integer.class,
                jti);
        return count != null && count > 0;
    }
}
