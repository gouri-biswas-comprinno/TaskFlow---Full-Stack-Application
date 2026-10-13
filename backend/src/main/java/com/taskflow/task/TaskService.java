package com.taskflow.task;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import com.taskflow.persistence.TaskRecord;
import com.taskflow.persistence.TaskRepository;

@Service
public class TaskService {

    private static final Set<String> UPDATE_FIELDS = Set.of("title", "description", "completed");

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskResponse create(UUID ownerId, CreateTaskRequest request) {
        Instant now = Instant.now();
        TaskRecord task = new TaskRecord(
                UUID.randomUUID(),
                ownerId,
                request.title().trim(),
                normalizeDescription(request.description()),
                false,
                now,
                now);
        taskRepository.save(task);
        return toResponse(task);
    }

    public List<TaskResponse> list(UUID ownerId) {
        return taskRepository.findAllByOwnerId(ownerId).stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse get(UUID id, UUID ownerId) {
        return taskRepository.findByIdAndOwnerId(id, ownerId)
                .map(this::toResponse)
                .orElseThrow(TaskNotFoundException::new);
    }

    public TaskResponse update(UUID id, UUID ownerId, JsonNode request) {
        if (request == null || !request.isObject() || request.isEmpty()) {
            throw new InvalidTaskRequestException();
        }
        request.fieldNames().forEachRemaining(field -> {
            if (!UPDATE_FIELDS.contains(field)) {
                throw new InvalidTaskRequestException();
            }
        });

        TaskRecord current = taskRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(TaskNotFoundException::new);
        String title = current.title();
        String description = current.description();
        boolean completed = current.completed();

        if (request.has("title")) {
            JsonNode value = request.get("title");
            if (!value.isTextual() || value.asText().isBlank() || value.asText().length() > 200) {
                throw new InvalidTaskRequestException();
            }
            title = value.asText().trim();
        }
        if (request.has("description")) {
            JsonNode value = request.get("description");
            if (!value.isNull() && (!value.isTextual() || value.asText().length() > 2000)) {
                throw new InvalidTaskRequestException();
            }
            description = value.isNull() ? null : normalizeDescription(value.asText());
        }
        if (request.has("completed")) {
            JsonNode value = request.get("completed");
            if (!value.isBoolean()) {
                throw new InvalidTaskRequestException();
            }
            completed = value.booleanValue();
        }

        TaskRecord updated = new TaskRecord(
                current.id(),
                current.ownerId(),
                title,
                description,
                completed,
                current.createdAt(),
                Instant.now());
        if (!taskRepository.updateByIdAndOwnerId(updated)) {
            throw new TaskNotFoundException();
        }
        return toResponse(updated);
    }

    public void delete(UUID id, UUID ownerId) {
        if (!taskRepository.deleteByIdAndOwnerId(id, ownerId)) {
            throw new TaskNotFoundException();
        }
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }

    private TaskResponse toResponse(TaskRecord task) {
        return new TaskResponse(
                task.id(),
                task.title(),
                task.description(),
                task.completed(),
                task.createdAt(),
                task.updatedAt());
    }
}
