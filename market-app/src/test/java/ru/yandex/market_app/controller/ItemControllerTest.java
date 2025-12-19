package ru.yandex.market_app.controller;

import java.util.UUID;

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
import ru.yandex.market_app.model.enums.EActionType;
import ru.yandex.market_app.model.enums.ESortType;
import ru.yandex.market_app.util.DataFactory;


@Tag("integration")
@Testcontainers
@ImportTestcontainers({DatabaseContainerTest.class, RedisTestContainer.class})
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ItemControllerTest extends DataFactory {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void getItemsTest() throws Exception {
        var mockRequest = UriComponentsBuilder.fromUriString("/items")
            .queryParam("sort", ESortType.PRICE.name())
            .queryParam("pageNumber", "1")
            .queryParam("pageSize", "5")
            .build()
            .toUri();

        var mockBadRequest = UriComponentsBuilder.fromUriString("/items")
            .queryParam("sort", ESortType.PRICE.name())
            .queryParam("pageNumber", "0")
            .queryParam("pageSize", "0")
            .build()
            .toUri();

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
    }

    @Test
    public void getItemByIdTest() throws Exception {
        webTestClient.get()
            .uri("/items/{id}", mockItem.getId())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);

        webTestClient.get()
            .uri("/items/{id}", 0L)
            .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);

        webTestClient.get()
            .uri("/items/{id}", 999L)
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    public void updateCartTest() throws Exception {
        var mockRequest = UriComponentsBuilder.fromUriString("/items/{id}")
            .queryParam("action", EActionType.PLUS.name())
            .build(mockItem.getId());

        var mockBadRequest = UriComponentsBuilder.fromUriString("/items/{id}")
            .queryParam("action", UUID.randomUUID().toString())
            .build(mockItem.getId());

        webTestClient.post()
            .uri(mockRequest)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);

        webTestClient.post()
            .uri(mockBadRequest)
            .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    public void updateCartWithRedirectTest() throws Exception {
        var mockRequest = UriComponentsBuilder.fromUriString("/items")
            .queryParam("id", mockItem.getId().toString())
            .queryParam("action", EActionType.PLUS.name())
            .build()
            .toUri();

        var mockBadRequest = UriComponentsBuilder.fromUriString("/items")
            .queryParam("id", mockItem.getId().toString())
            .queryParam("action", EActionType.PLUS.name())
            .queryParam("pageNumber", "0L")
            .queryParam("pageSize", "0L")
            .build()
            .toUri();

        webTestClient.post()
            .uri(mockRequest)
            .exchange()
            .expectStatus().is3xxRedirection();

        webTestClient.post()
            .uri(mockBadRequest)
            .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }
}
