package com.jcaa.usersmanagement.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.jcaa.usersmanagement.infrastructure.adapter.email.BrevoConfig;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@DisplayName("BrevoSpringConfig")
class BrevoSpringConfigTest {

  @Test
  @DisplayName("construye BrevoConfig y el RestClient con la clave API")
  void shouldCreateBrevoClientFromProperties() {
    final BrevoSpringConfig configuration = new BrevoSpringConfig();

    final BrevoConfig config =
        configuration.brevoConfig(
            "https://api.brevo.com",
            "xkeysib-test-api-key",
            "noreply@test.local",
            "Gestion de Usuarios",
            Duration.ofMillis(100),
            Duration.ofMillis(100),
            1,
            Duration.ofMillis(1),
            2.0,
            50,
            10,
            5,
            Duration.ofSeconds(1));
    final RestClient client = configuration.brevoRestClient(config);

    assertNotNull(config);
    assertNotNull(client);
  }
}
