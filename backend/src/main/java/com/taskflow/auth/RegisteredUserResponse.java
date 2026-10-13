package com.taskflow.auth;

import java.util.UUID;

public record RegisteredUserResponse(UUID id, String email) {
}
