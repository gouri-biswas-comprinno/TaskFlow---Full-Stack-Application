package com.taskflow.config;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class NeonDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String NEON_PROPERTY_SOURCE = "neonDatabaseConnection";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String pooledUrl = environment.getProperty("DATABASE_URL");
        if (pooledUrl == null || pooledUrl.isBlank()) {
            return;
        }

        DatabaseConnection pooled = parseConnection(pooledUrl);
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("spring.datasource.url", pooled.jdbcUrl());
        properties.put("spring.datasource.username", pooled.username());
        properties.put("spring.datasource.password", pooled.password());

        String unpooledUrl = environment.getProperty("DATABASE_URL_UNPOOLED");
        if (unpooledUrl != null && !unpooledUrl.isBlank()) {
            DatabaseConnection unpooled = parseConnection(unpooledUrl);
            properties.put("spring.flyway.url", unpooled.jdbcUrl());
            properties.put("spring.flyway.user", unpooled.username());
            properties.put("spring.flyway.password", unpooled.password());
        }

        environment.getPropertySources().addLast(new MapPropertySource(NEON_PROPERTY_SOURCE, properties));
    }

    static DatabaseConnection parseConnection(String connectionString) {
        URI uri;
        try {
            uri = URI.create(connectionString);
        } catch (IllegalArgumentException exception) {
            throw invalidConnectionString();
        }

        String scheme = uri.getScheme();
        String rawUserInfo = uri.getRawUserInfo();
        String rawPath = uri.getRawPath();
        if (!("postgres".equals(scheme) || "postgresql".equals(scheme))
                || uri.getHost() == null
                || rawUserInfo == null
                || !rawUserInfo.contains(":")
                || rawPath == null
                || rawPath.length() < 2) {
            throw invalidConnectionString();
        }

        String[] credentials = rawUserInfo.split(":", 2);
        String username;
        String password;
        try {
            username = decode(credentials[0]);
            password = decode(credentials[1]);
        } catch (IllegalArgumentException exception) {
            throw invalidConnectionString();
        }
        if (username.isBlank() || password.isBlank()) {
            throw invalidConnectionString();
        }

        Map<String, String> queryParameters = parseQuery(uri.getRawQuery());
        String sslMode = queryParameters.get("sslmode");
        if (sslMode != null
                && !("require".equals(sslMode)
                        || "verify-ca".equals(sslMode)
                        || "verify-full".equals(sslMode))) {
            throw new IllegalStateException("Neon PostgreSQL connections must require TLS.");
        }
        queryParameters.putIfAbsent("sslmode", "require");

        String host = uri.getHost();
        if (host.contains(":")) {
            host = "[" + host + "]";
        }
        String authority = host + (uri.getPort() < 0 ? "" : ":" + uri.getPort());
        String query = queryParameters.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .reduce((left, right) -> left + "&" + right)
                .orElse("");
        String jdbcUrl = "jdbc:postgresql://" + authority + rawPath + "?" + query;

        return new DatabaseConnection(jdbcUrl, username, password);
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> parameters = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return parameters;
        }

        try {
            for (String parameter : rawQuery.split("&")) {
                String[] parts = parameter.split("=", 2);
                String key = decode(parts[0]);
                String value = parts.length == 2 ? decode(parts[1]) : "";
                if ("channel_binding".equals(key)) {
                    key = "channelBinding";
                }
                parameters.put(key, value);
            }
        } catch (IllegalArgumentException exception) {
            throw invalidConnectionString();
        }
        return parameters;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static IllegalStateException invalidConnectionString() {
        return new IllegalStateException(
                "Neon database URL must be a valid PostgreSQL URI with database credentials.");
    }

    record DatabaseConnection(String jdbcUrl, String username, String password) {
    }
}
