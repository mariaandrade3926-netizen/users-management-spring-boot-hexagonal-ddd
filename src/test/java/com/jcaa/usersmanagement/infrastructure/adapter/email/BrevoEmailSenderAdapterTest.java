package com.jcaa.usersmanagement.infrastructure.adapter.email;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.jcaa.usersmanagement.domain.exception.EmailSenderException;
import com.jcaa.usersmanagement.domain.model.EmailDestinationModel;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@DisplayName("BrevoEmailSenderAdapter")
class BrevoEmailSenderAdapterTest {

  private static final String BASE_URL = "https://api.brevo.com";
  private static final String SEND_URL = BASE_URL + "/v3/smtp/email";

  private final EmailDestinationModel destination =
      new EmailDestinationModel("john@example.com", "John Doe", "Cuenta creada", "<p>Hola</p>");

  @Test
  @DisplayName("send() acepta el correo cuando Brevo responde 200")
  void shouldSendWhenBrevoAcceptsTheMessage() {
    final Harness harness = harness(1, 5, Duration.ofSeconds(30));
    harness.server.expect(requestTo(SEND_URL)).andExpect(method(HttpMethod.POST)).andRespond(withSuccess());

    harness.adapter.send(destination);

    harness.server.verify();
  }

  @Test
  @DisplayName("send() reintenta un 429 y termina cuando Brevo acepta el correo")
  void shouldRetryWhenBrevoRespondsTooManyRequests() {
    final Harness harness = harness(2, 5, Duration.ofSeconds(30));
    harness.server
        .expect(requestTo(SEND_URL))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
    harness.server.expect(requestTo(SEND_URL)).andExpect(method(HttpMethod.POST)).andRespond(withSuccess());

    harness.adapter.send(destination);

    harness.server.verify();
  }

  @Test
  @DisplayName("send() no reintenta un 400 y reporta el codigo HTTP")
  void shouldNotRetryWhenBrevoRejectsTheRequest() {
    final Harness harness = harness(3, 5, Duration.ofSeconds(30));
    harness.server
        .expect(requestTo(SEND_URL))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST));

    final EmailSenderException exception =
        assertThrows(EmailSenderException.class, () -> harness.adapter.send(destination));

    assertTrue(exception.getMessage().contains("HTTP 400"));
    assertTrue(exception.getMessage().contains("john@example.com"));
    harness.server.verify();
  }

  @Test
  @DisplayName("send() abre el circuit breaker tras fallos 5xx consecutivos")
  void shouldOpenCircuitBreakerAfterRepeatedServerErrors() {
    final Harness harness = harness(1, 3, Duration.ofMinutes(1));
    for (int attempt = 0; attempt < 3; attempt++) {
      harness.server
          .expect(requestTo(SEND_URL))
          .andExpect(method(HttpMethod.POST))
          .andRespond(withServerError());
    }
    for (int attempt = 0; attempt < 3; attempt++) {
      final EmailSenderException exception =
          assertThrows(EmailSenderException.class, () -> harness.adapter.send(destination));
      assertTrue(exception.getMessage().contains("HTTP 500"));
    }

    final EmailSenderException opened =
        assertThrows(EmailSenderException.class, () -> harness.adapter.send(destination));

    assertTrue(opened.getMessage().contains("circuit breaker abierto"));
    harness.server.verify();
  }

  @Test
  @DisplayName("send() traduce un fallo de red a EmailSenderException")
  void shouldTranslateConnectionFailure() {
    final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofMillis(300));
    requestFactory.setReadTimeout(Duration.ofMillis(300));
    final RestClient client =
        RestClient.builder().baseUrl("http://127.0.0.1:1").requestFactory(requestFactory).build();
    final BrevoEmailSenderAdapter adapter =
        new BrevoEmailSenderAdapter(config("http://127.0.0.1:1", 1, 5, Duration.ofSeconds(30)), client);

    final EmailSenderException exception =
        assertThrows(EmailSenderException.class, () -> adapter.send(destination));

    assertTrue(exception.getMessage().contains("ResourceAccessException"));
  }

  private static Harness harness(
      final int maxAttempts, final int minimumCalls, final Duration openDuration) {
    final RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
    final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    final BrevoEmailSenderAdapter adapter =
        new BrevoEmailSenderAdapter(
            config(BASE_URL, maxAttempts, minimumCalls, openDuration), builder.build());
    return new Harness(adapter, server);
  }

  private static BrevoConfig config(
      final String baseUrl,
      final int maxAttempts,
      final int minimumCalls,
      final Duration openDuration) {
    return new BrevoConfig(
        baseUrl,
        "xkeysib-test-api-key",
        "noreply@test.local",
        "Gestion de Usuarios",
        Duration.ofMillis(300),
        Duration.ofMillis(300),
        maxAttempts,
        Duration.ofMillis(1),
        2.0,
        50,
        10,
        minimumCalls,
        openDuration);
  }

  private record Harness(BrevoEmailSenderAdapter adapter, MockRestServiceServer server) {}
}
