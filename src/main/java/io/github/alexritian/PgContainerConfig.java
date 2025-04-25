package io.github.alexritian;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

/**
 * @author Too_young
 */
@Configuration
@ConditionalOnProperty(name = "use.testcontainers", havingValue = "true")
public class PgContainerConfig {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PgContainerConfig.class);

    @Value("${container.name:postgres}")
    private String name;

    @Value("${container.tag:16.3}")
    private String tag;

    private PostgreSQLContainer<?> postgreSQLContainer;

    @Bean
    public PostgreSQLContainer<?> postgreSQLContainer() {
        log.info("Starting PostgreSQL container...");
        String container = name + ":" + tag;
        postgreSQLContainer = new PostgreSQLContainer<>(container)
                .withDatabaseName("test")
                .withUsername("test")
                .withPassword("test");
        postgreSQLContainer.start();
        return postgreSQLContainer;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource(PostgreSQLContainer<?> postgreSQLContainer) {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(postgreSQLContainer.getJdbcUrl())
                .username(postgreSQLContainer.getUsername())
                .password(postgreSQLContainer.getPassword())
                .build();
    }


    @PreDestroy
    public void stopContainer() {
        if (postgreSQLContainer != null) {
            postgreSQLContainer.stop();
        }
    }

}
