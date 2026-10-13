package com.taskflow.persistence;

import java.time.Instant;
import java.util.UUID;

public record TaskRecord(
        UUID id,
        UUID ownerId,
        String title,
        String description,
        boolean completed,
        Instant createdAt,
        Instant updatedAt) {
}
