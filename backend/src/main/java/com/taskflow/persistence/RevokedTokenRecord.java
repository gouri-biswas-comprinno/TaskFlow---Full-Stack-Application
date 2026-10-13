package com.taskflow.persistence;

import java.time.Instant;

public record RevokedTokenRecord(String jti, Instant expiresAt, Instant revokedAt) {
}
