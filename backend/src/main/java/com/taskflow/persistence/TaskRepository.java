package com.taskflow.persistence;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TaskRepository {

    private static final RowMapper<TaskRecord> TASK_MAPPER = (row, rowNumber) -> new TaskRecord(
            row.getObject("id", UUID.class),
            row.getObject("owner_id", UUID.class),
            row.getString("title"),
            row.getString("description"),
            row.getBoolean("completed"),
            row.getTimestamp("created_at").toInstant(),
            row.getTimestamp("updated_at").toInstant());

    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(TaskRecord task) {
        jdbcTemplate.update(
                """
                        INSERT INTO tasks
                            (id, owner_id, title, description, completed, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                task.id(),
                task.ownerId(),
                task.title(),
                task.description(),
                task.completed(),
                Timestamp.from(task.createdAt()),
                Timestamp.from(task.updatedAt()));
    }

    public Optional<TaskRecord> findByIdAndOwnerId(UUID id, UUID ownerId) {
        List<TaskRecord> tasks = jdbcTemplate.query(
                """
                        SELECT id, owner_id, title, description, completed, created_at, updated_at
                        FROM tasks
                        WHERE id = ? AND owner_id = ?
                        """,
                TASK_MAPPER,
                id,
                ownerId);
        return tasks.stream().findFirst();
    }
}
