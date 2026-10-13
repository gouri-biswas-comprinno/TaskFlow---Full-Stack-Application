package com.taskflow.auth;

import java.time.Instant;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.taskflow.persistence.RevokedTokenRecord;
import com.taskflow.persistence.RevokedTokenRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RevokedTokenRepository revokedTokenRepository;

    public AuthController(
            AuthenticationService authenticationService,
            RevokedTokenRepository revokedTokenRepository) {
        this.authenticationService = authenticationService;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisteredUserResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisteredUserResponse response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public AccessTokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Jwt jwt) {
        revokedTokenRepository.save(new RevokedTokenRecord(
                jwt.getId(),
                jwt.getExpiresAt(),
                Instant.now()));
    }
}
