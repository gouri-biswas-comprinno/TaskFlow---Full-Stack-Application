package com.taskflow.persistence;

import java.time.Instant;
import java.util.UUID;

public record UserRecord(UUID id, String email, String passwordHash, Instant createdAt) {
}
