package ru.yandex.market_app.controller;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import ru.yandex.market_app.container.DatabaseContainer;
import ru.yandex.market_app.model.enums.EActionType;
import ru.yandex.market_app.util.DataFactory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Testcontainers
@ImportTestcontainers(DatabaseContainer.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class CartControllerTest extends DataFactory {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getCartTest() throws Exception {
        mockMvc.perform(get("/cart/items"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }

    @Test
    public void updateCartTest() throws Exception {
        var mockRequest = post("/cart/items")
            .queryParam("id", mockItem.getId().toString())
            .queryParam("action", EActionType.PLUS.name());

        mockMvc.perform(mockRequest)
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }
}
