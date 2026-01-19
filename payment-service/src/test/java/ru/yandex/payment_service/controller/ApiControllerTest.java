package ru.yandex.payment_service.controller;

import java.math.BigDecimal;

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

import ru.yandex.model.PaymentRequest;
import ru.yandex.payment_service.container.DatabaseContainerTest;

@Tag("integration")
@Testcontainers
@ImportTestcontainers(DatabaseContainerTest.class)
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testPayment() throws Exception {
        var mockRequest = UriComponentsBuilder.fromUriString("/payment")
            .build()
            .toUri();

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(new BigDecimal(100L));

        webTestClient.post()
            .uri(mockRequest)
            .bodyValue(paymentRequest)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON);
    }
}
