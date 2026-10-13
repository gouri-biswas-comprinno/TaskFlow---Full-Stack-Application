package com.taskflow.task;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TaskApiIntegrationTest {

    private static final String TEST_JWT_SECRET = "taskflow-task-api-test-key-has-at-least-32-bytes";

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16.9-alpine")
            .withDatabaseName("taskflow_tasks_test")
            .withUsername("taskflow_test")
            .withPassword("taskflow_test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("taskflow.jwt.secret", () -> TEST_JWT_SECRET);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void clearIsolatedDatabase() {
        jdbcTemplate.update("DELETE FROM tasks");
        jdbcTemplate.update("DELETE FROM revoked_tokens");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void ownerCanCreateListGetUpdateAndDeleteTask() throws Exception {
        String ownerToken = createUserAndGetToken();
        String taskId = createTask(ownerToken, """
                {"title":"Prepare assessment","description":"  Check the API  "}
                """);

        mockMvc.perform(get("/api/tasks").header(AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(taskId))
                .andExpect(jsonPath("$[0].title").value("Prepare assessment"))
                .andExpect(jsonPath("$[0].ownerId").doesNotExist());

        mockMvc.perform(get("/api/tasks/{id}", taskId).header(AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Check the API"))
                .andExpect(jsonPath("$.completed").value(false));

        mockMvc.perform(patch("/api/tasks/{id}", taskId)
                        .header(AUTHORIZATION, bearer(ownerToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Review assessment","completed":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Review assessment"))
                .andExpect(jsonPath("$.description").value("Check the API"))
                .andExpect(jsonPath("$.completed").value(true));

        mockMvc.perform(patch("/api/tasks/{id}", taskId)
                        .header(AUTHORIZATION, bearer(ownerToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"description":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(org.hamcrest.Matchers.nullValue()));

        mockMvc.perform(delete("/api/tasks/{id}", taskId).header(AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/tasks/{id}", taskId).header(AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found"));
    }

    @Test
    void rejectsMissingBlankAndOversizedTaskFieldsAndInvalidUpdates() throws Exception {
        String token = createUserAndGetToken();
        String overlongTitle = "x".repeat(201);
        String overlongDescription = "x".repeat(2001);

        assertBadCreate(token, "{}");
        assertBadCreate(token, """
                {"title":"   "}
                """);
        assertBadCreate(token, """
                {"title":"%s"}
                """.formatted(overlongTitle));
        assertBadCreate(token, """
                {"title":"Valid title","description":"%s"}
                """.formatted(overlongDescription));

        String taskId = createTask(token, """
                {"title":"Valid title"}
                """);
        mockMvc.perform(patch("/api/tasks/{id}", taskId)
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Task validation failed"));
        mockMvc.perform(patch("/api/tasks/{id}", taskId)
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"completed":"yes"}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/tasks/{id}", taskId)
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"ownerId":"%s"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnauthenticatedAccessToEveryTaskEndpoint() throws Exception {
        UUID unknownId = UUID.randomUUID();
        mockMvc.perform(post("/api/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Unauthorized"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        mockMvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/tasks/{id}", unknownId)).andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/tasks/{id}", unknownId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Unauthorized"}
                                """))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/tasks/{id}", unknownId)).andExpect(status().isUnauthorized());
    }

    @Test
    void returnsDocumentedNotFoundShapeForUnknownTaskIds() throws Exception {
        String token = createUserAndGetToken();
        mockMvc.perform(get("/api/tasks/{id}", UUID.randomUUID()).header(AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task not found"))
                .andExpect(jsonPath("$.path").value(org.hamcrest.Matchers.startsWith("/api/tasks/")));
    }

    @Test
    void anotherUserCannotReadUpdateOrDeleteTaskAndCannotSeeItInTheirList() throws Exception {
        String ownerToken = createUserAndGetToken();
        String otherToken = createUserAndGetToken();
        String forgedOwnerId = jwtDecoder.decode(otherToken).getSubject();
        String taskId = createTask(ownerToken, """
                {"title":"Private task","ownerId":"%s"}
                """.formatted(forgedOwnerId));

        mockMvc.perform(get("/api/tasks").header(AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/tasks/{id}", taskId).header(AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found"));
        mockMvc.perform(patch("/api/tasks/{id}", taskId)
                        .header(AUTHORIZATION, bearer(otherToken))
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"title":"Stolen update"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found"));
        mockMvc.perform(delete("/api/tasks/{id}", taskId).header(AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found"));

        mockMvc.perform(get("/api/tasks/{id}", taskId).header(AUTHORIZATION, bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Private task"));
    }

    private void assertBadCreate(String token, String request) throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"));
    }

    private String createUserAndGetToken() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"taskflow-password"}
                                """.formatted(email)))
                .andExpect(status().isCreated());
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"taskflow-password"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String createTask(String token, String request) throws Exception {
        String response = mockMvc.perform(post("/api/tasks")
                        .header(AUTHORIZATION, bearer(token))
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode task = objectMapper.readTree(response);
        return task.get("id").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
