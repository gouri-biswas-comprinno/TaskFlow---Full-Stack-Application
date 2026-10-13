package com.taskflow.auth;

import java.time.Instant;

public record AccessTokenResponse(String accessToken, String tokenType, Instant expiresAt) {
}
