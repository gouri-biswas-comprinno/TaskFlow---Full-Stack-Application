package com.taskflow.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeonDatabaseEnvironmentPostProcessorTest {

    @Test
    void postProcessorIsRegisteredForSpringBootStartup() {
        var processorNames = SpringFactoriesLoader.loadFactoryNames(
                EnvironmentPostProcessor.class,
                getClass().getClassLoader());

        assertTrue(processorNames.contains(NeonDatabaseEnvironmentPostProcessor.class.getName()));
    }

    @Test
    void configuresPooledApplicationAndDirectMigrationConnections() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty(
                        "DATABASE_URL",
                        "postgresql://task%2Bflow:p%40ss@ep-pooler.example.neon.tech/taskflow"
                                + "?sslmode=require&channel_binding=require")
                .withProperty(
                        "DATABASE_URL_UNPOOLED",
                        "postgresql://task%2Bflow:p%40ss@ep.example.neon.tech/taskflow?sslmode=require");

        new NeonDatabaseEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());

        assertEquals(
                "jdbc:postgresql://ep-pooler.example.neon.tech/taskflow"
                        + "?sslmode=require&channelBinding=require",
                environment.getProperty("spring.datasource.url"));
        assertEquals("task+flow", environment.getProperty("spring.datasource.username"));
        assertEquals("p@ss", environment.getProperty("spring.datasource.password"));
        assertEquals(
                "jdbc:postgresql://ep.example.neon.tech/taskflow?sslmode=require",
                environment.getProperty("spring.flyway.url"));
        assertEquals("task+flow", environment.getProperty("spring.flyway.user"));
        assertEquals("p@ss", environment.getProperty("spring.flyway.password"));
    }

    @Test
    void requiresTlsAndDoesNotExposeConnectionCredentialsInErrors() {
        String secret = "do-not-leak";

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> NeonDatabaseEnvironmentPostProcessor.parseConnection(
                        "postgresql://user:" + secret + "@db.example.neon.tech/taskflow?sslmode=disable"));

        assertFalse(exception.getMessage().contains(secret));
    }

    @Test
    void explicitSpringDatasourceSettingsTakePrecedence() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgresql://user:password@ep.example.neon.tech/taskflow")
                .withProperty("spring.datasource.url", "jdbc:postgresql://db:5432/taskflow")
                .withProperty("spring.datasource.username", "local-user")
                .withProperty("spring.datasource.password", "local-password");

        new NeonDatabaseEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());

        assertEquals("jdbc:postgresql://db:5432/taskflow", environment.getProperty("spring.datasource.url"));
        assertEquals("local-user", environment.getProperty("spring.datasource.username"));
        assertEquals("local-password", environment.getProperty("spring.datasource.password"));
    }
}
