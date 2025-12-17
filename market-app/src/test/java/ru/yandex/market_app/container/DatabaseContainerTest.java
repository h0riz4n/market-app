package ru.yandex.market_app.container;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

import org.testcontainers.junit.jupiter.Container;

public final class DatabaseContainerTest {
    
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.4")
        .withDatabaseName("test_db")
        .withUsername("test")
        .withPassword("test");
}
