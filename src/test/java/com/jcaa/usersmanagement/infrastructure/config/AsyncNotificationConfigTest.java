package com.jcaa.usersmanagement.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.concurrent.Executor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@DisplayName("AsyncNotificationConfig")
class AsyncNotificationConfigTest {

  @Test
  @DisplayName("el executor de notificaciones queda inicializado")
  void shouldCreateNotificationExecutor() {
    final Executor executor = new AsyncNotificationConfig().notificationExecutor();

    assertInstanceOf(ThreadPoolTaskExecutor.class, executor);
    ((ThreadPoolTaskExecutor) executor).shutdown();
  }
}
