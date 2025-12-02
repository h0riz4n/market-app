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
import ru.yandex.market_app.model.enums.EActionType;
import ru.yandex.market_app.util.DataFactory;

@Tag("integration")
@Testcontainers
@ImportTestcontainers(DatabaseContainerTest.class)
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CartControllerTest extends DataFactory {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void getCartTest() throws Exception {
        webTestClient.get()
            .uri("/cart/items")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
    }

    @Test
    public void updateCartTest() throws Exception {
        var mockRequest = UriComponentsBuilder.fromUriString("/cart/items")
            .queryParam("id", mockItem.getId().toString())
            .queryParam("action", EActionType.PLUS.name())
            .build()
            .toUri();

        webTestClient.post()
            .uri(mockRequest)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
    }
}
