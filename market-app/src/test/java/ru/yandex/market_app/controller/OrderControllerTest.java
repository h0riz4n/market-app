package ru.yandex.market_app.controller;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

import ru.yandex.market_app.container.DatabaseContainerTest;
import ru.yandex.market_app.container.RedisTestContainer;
import ru.yandex.market_app.util.DataFactory;

@Tag("integration")
@Testcontainers
@ImportTestcontainers({DatabaseContainerTest.class, RedisTestContainer.class})
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerTest extends DataFactory {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void getAllTest() throws Exception {
        webTestClient.get()
            .uri("/orders")
            .exchange()
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
    }

    @Test
    public void getOrderById() throws Exception {
        var mockRequest = UriComponentsBuilder.fromUriString("/orders/{id}")
            .queryParam("newOrder", "false")
            .build(mockOrder.getId());

        var mockBadRequest = UriComponentsBuilder.fromUriString("/orders/{id}")
            .build(0L);
        
        var mockNotFoundRequest = UriComponentsBuilder.fromUriString("/orders/{id}")
            .build(999L);

        webTestClient.get()
            .uri(mockRequest)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
        
        webTestClient.get()
            .uri(mockBadRequest)
            .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);

        webTestClient.get()
            .uri(mockNotFoundRequest)
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }
}
