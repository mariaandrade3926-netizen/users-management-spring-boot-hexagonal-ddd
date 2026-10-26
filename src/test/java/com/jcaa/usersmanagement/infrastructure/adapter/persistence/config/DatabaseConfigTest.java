package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DatabaseConfig")
class DatabaseConfigTest {

  @Test
  @DisplayName("sin modo SSL usa DISABLED para el entorno local")
  void shouldDisableSslByDefault() {
    final String jdbcUrl = new DatabaseConfig("localhost", 3306, "crud_usuarios", "root", "secret").buildJdbcUrl();

    assertTrue(jdbcUrl.contains("sslMode=DISABLED"));
    assertTrue(jdbcUrl.startsWith("jdbc:mysql://localhost:3306/crud_usuarios"));
  }

  @Test
  @DisplayName("respeta DB_SSL_MODE cuando el despliegue exige TLS")
  void shouldUseRequestedSslMode() {
    final String jdbcUrl =
        new DatabaseConfig("db.example.com", 4000, "crud_usuarios", "app", "secret", "REQUIRED")
            .buildJdbcUrl();

    assertTrue(jdbcUrl.contains("sslMode=REQUIRED"));
  }
}
