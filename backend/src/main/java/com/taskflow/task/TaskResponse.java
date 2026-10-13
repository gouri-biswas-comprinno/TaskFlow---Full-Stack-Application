package com.taskflow.task;

import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        boolean completed,
        Instant createdAt,
        Instant updatedAt) {
}
