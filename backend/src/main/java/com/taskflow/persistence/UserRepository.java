package com.taskflow.persistence;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private static final RowMapper<UserRecord> USER_MAPPER = (row, rowNumber) -> new UserRecord(
            row.getObject("id", java.util.UUID.class),
            row.getString("email"),
            row.getString("password_hash"),
            row.getTimestamp("created_at").toInstant());

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(UserRecord user) {
        jdbcTemplate.update(
                "INSERT INTO users (id, email, password_hash, created_at) VALUES (?, ?, ?, ?)",
                user.id(),
                user.email(),
                user.passwordHash(),
                Timestamp.from(user.createdAt()));
    }

    public Optional<UserRecord> findByEmail(String email) {
        List<UserRecord> users = jdbcTemplate.query(
                "SELECT id, email, password_hash, created_at FROM users WHERE email = ?",
                USER_MAPPER,
                email);
        return users.stream().findFirst();
    }
}
