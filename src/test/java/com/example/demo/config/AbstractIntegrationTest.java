package com.example.demo.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base infrastructure class for full Spring Boot integration tests.
 * Uses @Testcontainers(disabledWithoutDocker = true) so tests run cleanly
 * when Docker is available and skip gracefully when Docker daemon is offline.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractIntegrationTest {

    @Container
    public static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("upi_test_db")
                    .withUsername("test_user")
                    .withPassword("test_password");

    @Container
    public static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    @Container
    public static final KafkaContainer KAFKA_CONTAINER =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void setDynamicProperties(DynamicPropertyRegistry registry) {
        if (POSTGRES_CONTAINER.isRunning()) {
            registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
            registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        }
        if (REDIS_CONTAINER.isRunning()) {
            registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
            registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
        }
        if (KAFKA_CONTAINER.isRunning()) {
            registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        }
    }
}
