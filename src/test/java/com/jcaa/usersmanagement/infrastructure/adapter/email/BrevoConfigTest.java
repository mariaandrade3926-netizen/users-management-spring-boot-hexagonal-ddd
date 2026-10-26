package com.jcaa.usersmanagement.infrastructure.adapter.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BrevoConfig")
class BrevoConfigTest {

  @Test
  @DisplayName("acepta una clave API REST con prefijo xkeysib-")
  void shouldAcceptRestApiKey() {
    final BrevoConfig config = validConfig("xkeysib-test-api-key");

    assertEquals("https://api.brevo.com", config.baseUrl());
    assertEquals("noreply@test.local", config.fromAddress());
  }

  @Test
  @DisplayName("rechaza una clave que no es de la API REST")
  void shouldRejectKeyWithoutRestPrefix() {
    final IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> validConfig("smtp-key"));

    assertTrue(exception.getMessage().contains("xkeysib-"));
  }

  @Test
  @DisplayName("rechaza remitente vacio")
  void shouldRejectBlankSender() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new BrevoConfig(
                "https://api.brevo.com",
                "xkeysib-test-api-key",
                " ",
                "Gestion de Usuarios",
                Duration.ofSeconds(2),
                Duration.ofSeconds(5),
                3,
                Duration.ofMillis(500),
                2.0,
                50,
                10,
                5,
                Duration.ofSeconds(30)));
  }

  private static BrevoConfig validConfig(final String apiKey) {
    return new BrevoConfig(
        "https://api.brevo.com",
        apiKey,
        "noreply@test.local",
        "Gestion de Usuarios",
        Duration.ofSeconds(2),
        Duration.ofSeconds(5),
        3,
        Duration.ofMillis(500),
        2.0,
        50,
        10,
        5,
        Duration.ofSeconds(30));
  }
}
