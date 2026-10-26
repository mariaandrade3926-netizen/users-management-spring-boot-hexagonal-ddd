package com.jcaa.usersmanagement.infrastructure.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class DataSourceSpringConfig {

  private static final String LOG_DATASOURCE_INIT =
      "[DataSourceSpringConfig] DataSource inicializado. url={}";

  @Value("${db.url}")
  private String dbUrl;

  @Value("${db.username}")
  private String dbUsername;

  @Value("${db.password}")
  private String dbPassword;

  @Bean
  public DataSource dataSource() {
    final HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(dbUrl);
    hikariConfig.setUsername(dbUsername);
    hikariConfig.setPassword(dbPassword);
    hikariConfig.setMaximumPoolSize(10);
    hikariConfig.setMinimumIdle(2);
    hikariConfig.setConnectionTimeout(30_000);

    log.info(LOG_DATASOURCE_INIT, dbUrl);
    return new HikariDataSource(hikariConfig);
  }
}

