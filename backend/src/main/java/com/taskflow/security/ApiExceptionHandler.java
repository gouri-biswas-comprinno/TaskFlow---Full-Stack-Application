package com.taskflow.security;

import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.taskflow.auth.EmailAlreadyRegisteredException;
import com.taskflow.auth.InvalidCredentialsException;
import com.taskflow.task.InvalidTaskRequestException;
import com.taskflow.task.TaskNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiErrorResponse> emailAlreadyRegistered(HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "Email is already registered", request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> invalidCredentials(HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "Invalid email or password", request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiErrorResponse> invalidRequest(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Request validation failed", request);
    }

    @ExceptionHandler(InvalidTaskRequestException.class)
    public ResponseEntity<ApiErrorResponse> invalidTaskRequest(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Task validation failed", request);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> taskNotFound(HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "Task not found", request);
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status,
            String message,
            HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()));
    }
}
