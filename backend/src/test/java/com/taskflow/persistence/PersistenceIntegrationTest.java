package com.taskflow.persistence;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class PersistenceIntegrationTest {

    private static final String TEST_JWT_SECRET = "taskflow-test-signing-key-with-32-bytes-minimum";

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16.9-alpine")
            .withDatabaseName("taskflow_test")
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
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Test
    void migrationCreatesTheThreePlannedTables() {
        Integer tableCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name IN ('users', 'tasks', 'revoked_tokens')
                        """,
                Integer.class);

        assertEquals(3, tableCount);
        assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM flyway_schema_history", Integer.class));
    }

    @Test
    void userEmailMustBeUnique() {
        Instant now = Instant.now();
        userRepository.save(new UserRecord(UUID.randomUUID(), "person@example.com", "hash-one", now));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.save(
                        new UserRecord(UUID.randomUUID(), "person@example.com", "hash-two", now)));
    }

    @Test
    void taskRequiresAnExistingOwnerAndIsQueriedByOwner() {
        Instant now = Instant.now();
        UUID ownerId = UUID.randomUUID();
        UUID otherOwnerId = UUID.randomUUID();
        userRepository.save(new UserRecord(ownerId, "owner@example.com", "hash", now));
        userRepository.save(new UserRecord(otherOwnerId, "other@example.com", "hash", now));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> taskRepository.save(new TaskRecord(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Invalid owner task",
                        null,
                        false,
                        now,
                        now)));

        UUID taskId = UUID.randomUUID();
        TaskRecord task = new TaskRecord(taskId, ownerId, "Owned task", null, false, now, now);
        taskRepository.save(task);

        assertTrue(taskRepository.findByIdAndOwnerId(taskId, ownerId).isPresent());
        assertFalse(taskRepository.findByIdAndOwnerId(taskId, otherOwnerId).isPresent());
    }

    @Test
    void revokedTokenIdentifierMustBeUniqueAndCanBeLookedUp() {
        Instant now = Instant.now();
        revokedTokenRepository.save(new RevokedTokenRecord("token-id", now.plusSeconds(60), now));

        assertTrue(revokedTokenRepository.existsByJti("token-id"));
        assertFalse(revokedTokenRepository.existsByJti("another-token-id"));
        assertThrows(
                DataIntegrityViolationException.class,
                () -> revokedTokenRepository.save(
                        new RevokedTokenRecord("token-id", now.plusSeconds(120), now)));
    }
}
