package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

public record DatabaseConfig(
    String host, int port, String databaseName, String username, String password, String sslMode) {

  private static final String URL_TEMPLATE =
      "jdbc:mysql://%s:%d/%s?sslMode=%s&serverTimezone=UTC&allowPublicKeyRetrieval=true";

  public DatabaseConfig(
      final String host,
      final int port,
      final String databaseName,
      final String username,
      final String password) {
    this(host, port, databaseName, username, password, "DISABLED");
  }

  public String buildJdbcUrl() {
    final String mode = (sslMode == null || sslMode.isBlank()) ? "DISABLED" : sslMode.trim();
    return String.format(URL_TEMPLATE, host, port, databaseName, mode);
  }
}
